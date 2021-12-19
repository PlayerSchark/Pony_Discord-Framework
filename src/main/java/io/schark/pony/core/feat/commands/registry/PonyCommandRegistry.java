package io.schark.pony.core.feat.commands.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import io.schark.pony.Pony;
import io.schark.pony.core.PonyBot;
import io.schark.pony.core.feat.commands.annotation.Alias;
import io.schark.pony.core.feat.commands.annotation.access.AllowedChannels;
import io.schark.pony.core.feat.commands.annotation.access.AllowedGuilds;
import io.schark.pony.core.feat.commands.annotation.access.AllowedRoles;
import io.schark.pony.core.feat.commands.annotation.access.AllowedUsers;
import io.schark.pony.core.feat.commands.annotation.impl.PonyAccessor;
import io.schark.pony.core.feat.commands.annotation.impl.PonyRunnable;
import io.schark.pony.core.feat.commands.annotation.options.*;
import io.schark.pony.core.feat.commands.command.CommandInfo;
import io.schark.pony.core.feat.commands.command.GuildCommandInfo;
import io.schark.pony.core.feat.commands.executor.*;
import io.schark.pony.core.feat.commands.in.PonyChatLabel;
import io.schark.pony.exception.CommandRegisterException;
import io.schark.pony.utils.PonyUtils;
import lombok.Getter;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.internal.utils.tuple.ImmutablePair;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Player_Schark
 */
@Getter
public class PonyCommandRegistry {

	private volatile PonyBot bot;
	private volatile boolean noCommands;
	private volatile BiMap<RegistryLabel, PonyChatCommandExecutor> chatCommands = HashBiMap.create();
	private volatile BiMap<GuildRegistryLabel, PonyGuildCommandExecutable> guildCommands = HashBiMap.create();
	private volatile boolean isShutdown = false;
	private volatile boolean isShuttingDown;

	private <E extends PonyChatCommandExecutor> void reflectExecutors() { //reflections access <- visibility
		new Thread(()->{
		this.bot = Pony.getInstance().getPonyBot();
		try {
			String executorPackage = Pony.getInstance().getConfig().getCommandPackage();
			if (executorPackage == null) {
				this.noCommands = true;
				return;
			}
			Set<Class<E>> executor = this.getSubTypes(executorPackage);
			this.registerCommands(executor);
		}
		catch (Exception e) {
			throw new CommandRegisterException(e);
		}
	}, "Command Registry").start();
	}

	@SuppressWarnings("unchecked")
	private <E extends PonyCommandExecutable> Set<Class<E>> getSubTypes(String commandPackage) {
		Reflections reflections = new Reflections(commandPackage);
		Set<Class<? extends PonyCommandExecutor>> subTypes = reflections.getSubTypesOf(PonyCommandExecutor.class);

		Set<Class<E>> types = new HashSet<>();
		for (Class<? extends PonyCommandExecutor> subType : subTypes) {
			types.add((Class<E>) subType);
		}

		return types;
	}

	private <E extends PonyCommandExecutable> void registerCommands(Set<Class<E>> commands) {
		System.out.println("CommandRegistry: Awaiting JDA Ready");
		try {
			Pony.getInstance().awaitReady();
		}
		catch (InterruptedException e) {
			throw new CommandRegisterException(e);
		}
		List<Class> blackListed =Arrays.asList(PonyChatCommandExecutor.class, PonyGuildCommandExecutor.class);
		for (Class<E> commandClass : commands) {
			if (blackListed.contains(commandClass)) {
				continue;
			}
			try {
				System.out.println("Starting registering executor '" + commandClass.getSimpleName() + "'");
				Constructor<E> constructor = commandClass.getConstructor();
				Pair<RegistryLabel, E> registry = this.reflectAnnotations(constructor, commandClass);
				this.put(registry);

				new Thread(()->{
					System.out.println("DiscordCommands: Registering");
					RegistryLabel label = registry.getLeft();
					E executor = registry.getRight();
					switch (label) {
						case GuildRegistryLabel lbl -> this.registerGuildCommand(lbl, ((PonyGuildCommandExecutable) executor));
						case SlashRegistryLabel ignore -> this.registerSlashCommand(((PonyGuildCommandExecutable) executor));
					default -> throw new IllegalStateException("Unexpected value: " + label);
					}
				}).start();

			}
			catch (NoSuchMethodException e) {
				throw new CommandRegisterException(e);
			}
		}
	}

	private <E extends PonyGuildCommandExecutable> void registerGuildCommand(GuildRegistryLabel label, E executor) {
		for(Long guildId : label.getGuildIds()) {
			this.registerGuildCommand(executor, guildId);
		}
	}

	private <E extends PonyGuildCommandExecutable> void registerGuildCommand(E executor, Long guildId) {
		this.registerDiscordCommand(executor, this.bot.getGuildById(guildId)::updateCommands);
	}

	private <E extends PonyGuildCommandExecutable> void registerSlashCommand(E executor) {
		this.registerDiscordCommand(executor, this.bot.getJda()::updateCommands);
	}

	private <E extends PonyGuildCommandExecutable> void registerDiscordCommand(final E executor, Supplier<CommandListUpdateAction> resolver) {
		CommandListUpdateAction action = resolver.get();
		this.addCommandData(executor, action);
		action.queue(x-> System.out.println("DiscordCommand: Executor '" + executor.getRawLabel() + "' done."));
		System.out.println("DiscordCommand: Executor '" + executor.getRawLabel() + "' queued.");
	}

	private <E extends PonyGuildCommandExecutable> void addCommandData(E executor, CommandListUpdateAction action) {
		CommandData commandData = executor.getCommandData();
		OptionData single = executor.withSingleOptionData();
		if (single != null) {
			commandData.addOptions(single);
		}
		commandData.addOptions(executor.withOptionData());
		//noinspection ResultOfMethodCallIgnored
		action.addCommands(commandData);
	}

	private <E extends PonyCommandExecutable> void put(Pair<RegistryLabel,E> registry) {
		PonyCommandExecutable command = registry.getRight();
		RegistryLabel label = registry.getLeft();

		switch (command) {
			case PonyChatCommandExecutor executor -> {
				if (executor instanceof PonyGuildCommandExecutable guildExecutable) {
					this.guildCommands.put((GuildRegistryLabel) label, guildExecutable);
					System.out.println("Command " + label.getRawLabel() + " registered to guilds as chat.");
				}
				this.chatCommands.put(label, executor);
				System.out.println("Command " + label.getRawLabel() + " registered to chat.");
			}
			case PonyGuildCommandExecutor executor -> {
				this.guildCommands.put((GuildRegistryLabel) label, executor);
				System.out.println("Command " + label.getRawLabel() + " registered to guilds.");
			}
			default -> throw new IllegalStateException("Unexpected value: " + command);
		}
	}

	private <E extends PonyCommandExecutable> Pair<RegistryLabel, E> reflectAnnotations(Constructor<E> constructor, Class<E> commandClass) {
		E command = this.executorInstance(commandClass);
		CommandInfo commandInfo = this.generateInfo(constructor, command);
		Set<String> aliases = this.assignAnnotation(constructor, Alias.class, anno->Arrays.asList(anno.aliases()));

		return this.commandInstance(command, aliases, commandInfo);
	}

	private <E extends PonyCommandExecutable> CommandInfo generateInfo(Constructor<E> constructor, PonyCommandExecutable command) {
		//TODO: Many To Object
		Class<AllowedRoles> rolesClass = AllowedRoles.class;
		Class<AllowedUsers> usersClass = AllowedUsers.class;
		Class<AllowedGuilds> guildsClass = AllowedGuilds.class;
		Class<AllowedChannels> channelsClass = AllowedChannels.class;

		//TODO: Many To Object
		Set<Long> roles = this.assignAnnotation(constructor, rolesClass, anno->this.idsFromAcessor(command, anno, AllowedRoles::accessor, a->PonyUtils.toUpperLong(a.ids())));
		Set<Long> users = this.assignAnnotation(constructor, usersClass, anno->this.idsFromAcessor(command, anno, AllowedUsers::accessor, a->PonyUtils.toUpperLong(a.ids())));
		Set<Long> guilds = this.assignAnnotation(constructor, guildsClass, anno->this.idsFromAcessor(command, anno, AllowedGuilds::accessor, a->PonyUtils.toUpperLong(a.ids())));
		Set<Long> channels = this.assignAnnotation(constructor, channelsClass, anno->this.idsFromAcessor(command, anno, AllowedChannels::accessor, a->PonyUtils.toUpperLong(a.ids())));

		boolean botUsable = constructor.isAnnotationPresent(BotUsable.class);
		boolean sendTyping = constructor.isAnnotationPresent(SendTyping.class);
		boolean isCaseSensitive = constructor.isAnnotationPresent(CaseSensitive.class);

		//TODO: Many To Object
		PonyRunnable noRole = this.solveAnnotation(constructor, rolesClass, anno->this.instanceFunction(anno.noAccessFunction()));
		PonyRunnable noUser = this.solveAnnotation(constructor, usersClass, anno->this.instanceFunction(anno.noAccessFunction()));
		PonyRunnable noGuild = this.solveAnnotation(constructor, guildsClass, anno->this.instanceFunction(anno.noAccessFunction()));
		PonyRunnable noChannel = this.solveAnnotation(constructor, channelsClass, anno->this.instanceFunction(anno.noAccessFunction()));


		//TODO: Many To Object
		String noRoleMessage = this.solveAnnotation(constructor, rolesClass, AllowedRoles::noAccessMessage);
		String noUserMessage = this.solveAnnotation(constructor, usersClass, AllowedUsers::noAccessMessage);
		String noGuildMessage = this.solveAnnotation(constructor, guildsClass, AllowedGuilds::noAccessMessage);
		String noChannelMessage = this.solveAnnotation(constructor, channelsClass, AllowedChannels::noAccessMessage);

		CommandInfo createdInfo;

		if (command instanceof PonyGuildCommandExecutable executable) { //isGuildCommand
			boolean sendThinking = constructor.isAnnotationPresent(SendThinking.class);
			boolean isEphemeral = constructor.isAnnotationPresent(Ephemeral.class);
			guilds = this.idsFromAcessor(command, executable, PonyGuildCommandExecutable::accessor, ex->PonyUtils.toUpperLong(ex.guildIds()));
			createdInfo = new GuildCommandInfo(roles, users, guilds, channels,
												botUsable, isCaseSensitive, sendThinking, isEphemeral,
												noRole, noUser, noGuild, noChannel,
												noRoleMessage, noUserMessage, noGuildMessage, noChannelMessage);
		}
		else {
			createdInfo = new CommandInfo(roles, users, guilds, channels,
											botUsable, sendTyping, isCaseSensitive,
											noRole, noUser, noGuild, noChannel,
											noRoleMessage, noUserMessage, noGuildMessage, noChannelMessage);
		}

		return createdInfo;
	}

	private PonyRunnable instanceFunction(Class<? extends PonyRunnable> clazz) {
		try {
			return clazz.getConstructor().newInstance();
		}
		catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			throw new CommandRegisterException(e);
		}
	}

	private <T> Set<Long> idsFromAcessor(PonyCommandExecutable command, T target,
					Function<T, Class<? extends PonyAccessor>> accessorSolver,
					Function<T, Set<Long>> defaultSolver) {
		Set<Long> accessorIds = new HashSet<>();
		Class<? extends PonyAccessor> accessorClass = accessorSolver.apply(target);

		if (!PonyAccessor.class.equals(accessorClass)) {
			accessorIds.addAll(this.solveAccessorIds(command, accessorClass));
		}

		accessorIds.addAll(defaultSolver.apply(target));
		return accessorIds;
	}

	private Set<Long> solveAccessorIds(PonyCommandExecutable command, Class<? extends PonyAccessor> accessorClass) {
		try {
			PonyAccessor accessor = accessorClass.getConstructor().newInstance();
			Long[] accessorIds = accessor.getIds(command);
			return Sets.newHashSet(accessorIds);
		}
		catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new CommandRegisterException(e);
		}
	}

	/**
	 * uses the given getter to collect all found elements into a single set
	 *
	 * @param constructor constructor to read annotations from
	 * @param annotation class of the annotation to match
	 * @param getter function that needs to be executed for the given annotation
	 * @param <E> Executor type for the Command
	 * @param <T> input annotation
	 * @param <V> output object type
	 * @return solved annotation
	 */
	private <E extends PonyCommandExecutable, T extends Annotation, V> @NotNull Set<V> assignAnnotation(Constructor<E> constructor,
																							Class<T> annotation,
																							Function<T, Collection<V>> getter) {
		Set<V> result = Collections.emptySet();
		Collection<V> anno = this.solveAnnotation(constructor, annotation, getter);
		if (anno != null) {
			result = new HashSet<>(anno);
		}
		return result;
	}

	/**
	 * reads the values inside a annotation by executing the getter and applies the found values to the given set.
	 * Uses a stream to support primitive arrays. primitve types for V are not suppored because Stream#boxed() is not in the method.
	 *
	 * @param constructor constructor to read annotations from
	 * @param annotation annotation to read
	 * @param getter function that needs to be executed for the given annotation
	 * @param <E> Executor type for the Command
	 * @param <T> input annotation
	 * @param <V> ouput object type
	 * @return all values from an annotation
	 */
	@Nullable private <E, T extends Annotation, V> V solveAnnotation(Constructor<E> constructor,
																Class<T> annotation,
																Function<T, V> getter) {
		T anno = constructor.getAnnotation(annotation);
		if (anno == null) {
			return null;
		}
		return getter.apply(anno);
	}

	private <E extends PonyCommandExecutable> Pair<RegistryLabel, E> commandInstance(E command, Set<String> aliases, CommandInfo commandInfo) {
		String rawLabel = command.getRawLabel();
		if (!commandInfo.isCaseSensitive()) {
			rawLabel = rawLabel.toLowerCase(Locale.ROOT);
		}
		RegistryLabel label = commandInfo instanceof GuildCommandInfo guildInfo
						? new GuildRegistryLabel(rawLabel, aliases, guildInfo, guildInfo.getAllowedGuilds())
						: new RegistryLabel(rawLabel, aliases, commandInfo);
		return new ImmutablePair<>(label, command);
	}

	private <E extends PonyCommandExecutable> E executorInstance(Class<E> commandClass) {
		try {
			return commandClass.getConstructor().newInstance();
		}
		catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new CommandRegisterException(e);
		}
	}

	public boolean isRegistered(String label) {
		return Boolean.TRUE.equals(this.forChatCommands(reg->reg.matches(label), x->Boolean.TRUE));
	}

	@Nullable public PonyChatCommandExecutor getChatCommand(PonyChatLabel label) {
			return this.getChatCommand(label.getContent());
	}

	@Nullable public PonyChatCommandExecutor getChatCommand(String label) {
		return this.forChatCommands(lbl->lbl.matches(label), this::getChatCommand);
	}

	@Nullable public PonyChatCommandExecutor getChatCommand(RegistryLabel label) {
		return this.getPonyChatCommandExecutor(label);
	}

	public PonyGuildCommandExecutable getGuildCommand(String name) {
		return this.forGuildCommands(lbl->lbl.matches(name), this::getGuildCommand);
	}

	@Nullable public PonyGuildCommandExecutable getGuildCommand(GuildRegistryLabel label) {
		return this.getPonyGuildCommandExecutor(label);
	}

	@Nullable private PonyChatCommandExecutor getPonyChatCommandExecutor(RegistryLabel label) {
		return this.forChatCommands(reg->reg.equals(label), this.chatCommands::get);
	}

	@Nullable private PonyGuildCommandExecutable getPonyGuildCommandExecutor(GuildRegistryLabel label) {
		return this.forGuildCommands(reg -> reg.equals(label), x->this.guildCommands.get(label));
	}

	@Nullable private <R> R forChatCommands(Function<RegistryLabel, Boolean> matchIf, Function<RegistryLabel, R> result) {
		return this.forCommands(this.chatCommands, matchIf, result);
	}

	@Nullable private <R> R forGuildCommands(Function<GuildRegistryLabel, Boolean> matchIf, Function<GuildRegistryLabel, R> result) {
		return this.forCommands(this.guildCommands, matchIf, result);
	}

	@Nullable private <L extends RegistryLabel, E extends PonyCommandExecutable, R> R forCommands(Map<L, E> map, Function<L, Boolean> matchIf, Function<L, R> result) {
		for (L registryLabel : map.keySet()) {
			if (matchIf.apply(registryLabel)) {
				return result.apply(registryLabel);
			}
		}
		return null;
	}

	public RegistryLabel getLabel(PonyChatLabel label) {
		return this.forChatCommands(req->req.matches(label.getContent()), reg->reg);
	}

	private void runNoAccess(Message message, String noAccessMessage, PonyRunnable runnable) {
		if (runnable != null) {
			runnable.run();
		}
		if (noAccessMessage != null) {
			message.getChannel().sendMessage(noAccessMessage).queue();
		}
	}

	public boolean hasAccess(Message message, PonyChatCommandExecutor executor) {
		CommandInfo commandInfo = this.getcommandInfo(executor);
		boolean isPublic = executor.isGuildCommand();

		User author = message.getAuthor();
		if (author.isBot() && !commandInfo.isBotUsable()) {
			return false;
		}

		MessageChannel channel = message.getChannel();
		if (isPublic) {
			//if channel is no guild but command is a guild command we need to reject - ALWAYS
			if (channel instanceof GuildChannel guildChannel) {
				//guild check
				Set<Long> allowedGuilds = commandInfo.getAllowedGuilds();
				if (!allowedGuilds.isEmpty() && !allowedGuilds.contains(guildChannel.getGuild().getIdLong())) {
					this.runNoAccess(message, commandInfo.getNoGuildMessage(), commandInfo.getNoGuild());
					return false;
				}

				//role check
				Member member = message.getMember();
				assert member != null;
				if (this.hasAccess(commandInfo.getAllowedRoles(), member)) {
					return true;
				}
				this.runNoAccess(message, commandInfo.getNoRoleMessage(), commandInfo.getNoRole());
			}
			else {
				return false;
			}
		}

		//user check
		if (this.hasUserAccess(message, commandInfo, author)) {
			return true;
		}

		//channels
		return this.hasChannelAccess(message, commandInfo, channel);
	}

	private boolean hasChannelAccess(Message message, CommandInfo commandInfo, MessageChannel channel) {
		Set<Long> allowedChannels = commandInfo.getAllowedChannels();
		boolean noChannel = allowedChannels.isEmpty() || allowedChannels.contains(channel.getIdLong());
		this.runNoAccess(message, commandInfo.getNoChannelMessage(), commandInfo.getNoChannel());
		return noChannel;
	}

	private boolean hasUserAccess(Message message, CommandInfo commandInfo, User author) {
		Set<Long> allowedUsers = commandInfo.getAllowedUsers();
		if (allowedUsers.isEmpty() || allowedUsers.contains(author.getIdLong())) {
			return true;
		}
		this.runNoAccess(message, commandInfo.getNoUserMessage(), commandInfo.getNoUser());
		return false;
	}

	private boolean hasAccess(Set<Long> roles, Member member) {
		if (roles.isEmpty()) {
			return true;
		}

		for (Role role : member.getRoles()) {
			if (roles.contains(role.getIdLong())) {
				return true;
			}
		}
		return false;
	}

	public CommandInfo getcommandInfo(PonyChatCommandExecutor executor) {
		RegistryLabel label = this.chatCommands.inverse().get(executor);
		return label.getCommandInfo();
	}

	public synchronized void shutdown() {
		if (this.isShuttingDown || this.isShutdown) {
			System.out.println("Registry is already stopped or is currently stopping.");
			return;
		}
		this.isShuttingDown = true;
		if (Pony.getInstance().getConfig().unregisterCommands()) {
			System.out.println("Shutting down Commands");
			for (GuildRegistryLabel label : this.guildCommands.keySet()) {
				System.out.println("Unregistering Command");
				Set<Long> guildIds = label.getGuildIds();
				for (Long guildId : guildIds) {
					Guild guildById = this.bot.getGuildById(guildId);
					guildById.updateCommands().complete();
				}
				System.out.println("Command unregistered");
			}
		}
		this.isShutdown = true;
	}
}