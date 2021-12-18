package io.schark.pony.core.feat.commands.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import io.schark.pony.Pony;
import io.schark.pony.core.PonyBot;
import io.schark.pony.core.feat.commands.annotation.Alias;
import io.schark.pony.core.feat.commands.annotation.GuildCommand;
import io.schark.pony.core.feat.commands.annotation.access.AllowedChannels;
import io.schark.pony.core.feat.commands.annotation.access.AllowedGuilds;
import io.schark.pony.core.feat.commands.annotation.access.AllowedRoles;
import io.schark.pony.core.feat.commands.annotation.access.AllowedUsers;
import io.schark.pony.core.feat.commands.annotation.impl.PonyAccessor;
import io.schark.pony.core.feat.commands.annotation.impl.PonyRunnable;
import io.schark.pony.core.feat.commands.annotation.options.*;
import io.schark.pony.core.feat.commands.command.CommandInfo;
import io.schark.pony.core.feat.commands.command.GuildCommandInfo;
import io.schark.pony.core.feat.commands.comp.PonyCommandComponents;
import io.schark.pony.core.feat.commands.executor.PonyChatCommandExecutor;
import io.schark.pony.core.feat.commands.executor.PonyCommandExecutor;
import io.schark.pony.core.feat.commands.executor.PonyGuildCommandExecutor;
import io.schark.pony.core.feat.commands.in.PonyLabel;
import io.schark.pony.exception.CommandRegisterException;
import io.schark.pony.utils.PonyUtils;
import lombok.Getter;
import net.dv8tion.jda.api.entities.*;
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

	private PonyBot bot;
	private boolean noCommands;
	private BiMap<RegistryLabel, PonyCommandExecutor> chatCommands = HashBiMap.create();
	private BiMap<GuildRegistryLabel, PonyCommandExecutor> guildCommands = HashBiMap.create();

	private <E extends PonyChatCommandExecutor> void reflectExecutors() { //reflections access <- visibility
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
	}

	@SuppressWarnings("unchecked")
	private <E extends PonyCommandExecutor> Set<Class<E>> getSubTypes(String commandPackage) {
		Reflections reflections = new Reflections(commandPackage);
		Set<Class<? extends PonyCommandExecutor>> subTypes = reflections.getSubTypesOf(PonyCommandExecutor.class);

		Set<Class<E>> types = new HashSet<>();
		for (Class<? extends PonyCommandExecutor> subType : subTypes) {
			types.add((Class<E>) subType);
		}

		return types;
	}

	private <E extends PonyCommandExecutor> void registerCommands(Set<Class<E>> commands) {
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
					System.out.println("DiscordCommands: Awaiting JDA Ready");
					try {
						Pony.getInstance().awaitReady();
					}
					catch (InterruptedException e) {
						throw new CommandRegisterException(e);
					}

					System.out.println("DiscordCommands: Registering");
					RegistryLabel label = registry.getLeft();
					switch (label) {
					case GuildRegistryLabel ignored -> this.registerGuildCommand(registry);
					case SlashRegistryLabel ignored -> this.registerSlashCommand(registry);
					default -> throw new IllegalStateException("Unexpected value: " + label);
					}
				}).start();

			}
			catch (NoSuchMethodException e) {
				throw new CommandRegisterException(e);
			}
		}
	}

	private <E extends PonyCommandExecutor> void registerGuildCommand(Pair<RegistryLabel, E> registry) {
		Set<Long> guildIds = ((GuildRegistryLabel) registry.getLeft()).getGuildIds();
		E executor = registry.getRight();

		for(Long guildId : guildIds) {
			this.registerGuildCommand(executor, guildId);
		}
	}

	private <E extends PonyCommandExecutor> void registerGuildCommand(E executor, Long guildId) {
		this.registerDiscordCommand(executor, this.bot.getGuildById(guildId)::updateCommands);
	}

	private <E extends PonyCommandExecutor> void registerSlashCommand(Pair<RegistryLabel, E> registry) {
		E executor = registry.getRight();
		this.registerDiscordCommand(executor, this.bot.getJda()::updateCommands);
	}

	private <E extends PonyCommandExecutor> void registerDiscordCommand(final E executor, Supplier<CommandListUpdateAction> resolver) {
		CommandListUpdateAction action = resolver.get();
		PonyCommandComponents components = executor.getComponents();
		//noinspection ResultOfMethodCallIgnored
		action.addCommands(components);
		action.queue(x-> System.out.println("DiscordCommand: Executor '" + executor.getRawLabel() + "' done."));
		System.out.println("DiscordCommand: Executor '" + executor.getRawLabel() + "' queued.");
	}

	private <E extends PonyCommandExecutor> void put(Pair<RegistryLabel,E> registry) {
		PonyCommandExecutor command = registry.getRight();
		RegistryLabel label = registry.getLeft();

		switch (command) {
			case PonyChatCommandExecutor executor -> {
				if (label instanceof GuildRegistryLabel guildLabel) {
					this.guildCommands.put(guildLabel, executor);
				}
				this.chatCommands.put(label, executor);
				System.out.println("Command " + label.getRawLabel() + " registered.");
			}
			case PonyGuildCommandExecutor executor -> this.guildCommands.put((GuildRegistryLabel) label, executor);
			default -> throw new IllegalStateException("Unexpected value: " + command);
		}
	}

	private <E extends PonyCommandExecutor> Pair<RegistryLabel, E> reflectAnnotations(Constructor<E> constructor, Class<E> commandClass) {
		E command = this.executorInstance(commandClass);
		CommandInfo commandInfo = this.generateInfo(constructor, command);
		Set<String> aliases = this.assignAnnotation(constructor, Alias.class, anno->Arrays.asList(anno.aliases()));

		return this.commandInstance(command, aliases, commandInfo);
	}

	private CommandInfo generateInfo(Constructor<? extends PonyCommandExecutor> constructor, PonyCommandExecutor command) {
		Class<AllowedRoles> rolesClass = AllowedRoles.class;
		Class<AllowedUsers> usersClass = AllowedUsers.class;
		Class<AllowedGuilds> guildsClass = AllowedGuilds.class;
		Class<AllowedChannels> channelsClass = AllowedChannels.class;
		Class<GuildCommand> guildCommandClass = GuildCommand.class;

		Set<Long> roles = this.assignAnnotation(constructor, rolesClass, anno->this.idsFromAcessor(command, anno, AllowedRoles::accessor, a->PonyUtils.toUpperLong(a.ids())));
		Set<Long> users = this.assignAnnotation(constructor, usersClass, anno->this.idsFromAcessor(command, anno, AllowedUsers::accessor, a->PonyUtils.toUpperLong(a.ids())));
		Set<Long> guilds = this.assignAnnotation(constructor, guildsClass, anno->this.idsFromAcessor(command, anno, AllowedGuilds::accessor, a->PonyUtils.toUpperLong(a.ids())));
		Set<Long> channels = this.assignAnnotation(constructor, channelsClass, anno->this.idsFromAcessor(command, anno, AllowedChannels::accessor, a->PonyUtils.toUpperLong(a.ids())));

		boolean botUsable = constructor.isAnnotationPresent(BotUsable.class);
		boolean sendTyping = constructor.isAnnotationPresent(SendTyping.class);
		boolean isCaseSensitive = constructor.isAnnotationPresent(CaseSensitive.class);
		boolean isGuildCommand = constructor.isAnnotationPresent(guildCommandClass);


		PonyRunnable noRole = this.solveAnnotation(constructor, rolesClass, anno->this.instanceFunction(anno.noAccessFunction()));
		PonyRunnable noUser = this.solveAnnotation(constructor, usersClass, anno->this.instanceFunction(anno.noAccessFunction()));
		PonyRunnable noGuild = this.solveAnnotation(constructor, guildsClass, anno->this.instanceFunction(anno.noAccessFunction()));
		PonyRunnable noChannel = this.solveAnnotation(constructor, channelsClass, anno->this.instanceFunction(anno.noAccessFunction()));


		String noRoleMessage = this.solveAnnotation(constructor, rolesClass, AllowedRoles::noAccessMessage);
		String noUserMessage = this.solveAnnotation(constructor, usersClass, AllowedUsers::noAccessMessage);
		String noGuildMessage = this.solveAnnotation(constructor, guildsClass, AllowedGuilds::noAccessMessage);
		String noChannelMessage = this.solveAnnotation(constructor, channelsClass, AllowedChannels::noAccessMessage);

		CommandInfo createdInfo;

		if (isGuildCommand) {
			boolean sendThinking = constructor.isAnnotationPresent(SendThinking.class);
			boolean isEphemeral = constructor.isAnnotationPresent(Ephemeral.class);
			guilds = this.assignAnnotation(constructor, guildCommandClass, anno->this.idsFromAcessor(command, anno, GuildCommand::accessor, a->PonyUtils.toUpperLong(a.guildIds())));
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

	private <T extends Annotation> Set<Long> idsFromAcessor(PonyCommandExecutor command, T anno,
					Function<T, Class<? extends PonyAccessor>> acFunc,
					Function<T, Set<Long>> anFunc) {
		Set<Long> accessorIds = new HashSet<>();
		Class<? extends PonyAccessor> accessorClass = acFunc.apply(anno);

		if (!PonyAccessor.class.equals(accessorClass)) {
			accessorIds.addAll(this.solveAccessorIds(command, accessorClass));
		}

		accessorIds.addAll(anFunc.apply(anno));
		return accessorIds;
	}

	private Set<Long> solveAccessorIds(PonyCommandExecutor command, Class<? extends PonyAccessor> accessorClass) {
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
	private <E extends PonyCommandExecutor, T extends Annotation, V> @NotNull Set<V> assignAnnotation(Constructor<E> constructor,
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

	private <E extends PonyCommandExecutor> Pair<RegistryLabel, E> commandInstance(E command, Set<String> aliases, CommandInfo commandInfo) {
		String rawLabel = command.getRawLabel();
		if (!commandInfo.isCaseSensitive()) {
			rawLabel = rawLabel.toLowerCase(Locale.ROOT);
		}
		RegistryLabel label = commandInfo instanceof GuildCommandInfo guildInfo
						? new GuildRegistryLabel(rawLabel, aliases, guildInfo, guildInfo.getAllowedGuilds())
						: new RegistryLabel(rawLabel, aliases, commandInfo);
		return new ImmutablePair<>(label, command);
	}

	private <E extends PonyCommandExecutor> E executorInstance(Class<E> commandClass) {
		try {
			return commandClass.getConstructor().newInstance();
		}
		catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new CommandRegisterException(e);
		}
	}

	public boolean isRegistered(String label) {
		return Boolean.TRUE.equals(this.forCommands(reg->reg.matches(label), x->Boolean.TRUE));
	}

	@Nullable public PonyChatCommandExecutor get(PonyLabel label) {
			return this.get(label.getContentRaw());
	}

	@Nullable public PonyChatCommandExecutor get(String label) {
		return this.forCommands(reg->reg.matches(label), this::get);
	}

	@Nullable public PonyChatCommandExecutor get(RegistryLabel label) {
		return this.getPonyCommandExecutor(label);
	}

	@Nullable private PonyChatCommandExecutor getPonyCommandExecutor(RegistryLabel label) {
		return (PonyChatCommandExecutor) this.forCommands(reg->reg.equals(label), this.chatCommands::get);
	}

	@Nullable  private <T> T forCommands(Function<RegistryLabel, Boolean> matchIf, Function<RegistryLabel, T> result) {
		for (RegistryLabel registryLabel : this.chatCommands.keySet()) {
			if (matchIf.apply(registryLabel)) {
				return result.apply(registryLabel);
			}
		}
		return null;
	}

	public RegistryLabel getLabel(PonyLabel label) {
		return this.forCommands(req->req.matches(label.getContentRaw()), reg->reg);
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
		System.out.println("Shutting down Commands");
		Set<Long> guilds = new HashSet<>();
		for (GuildRegistryLabel label : this.guildCommands.keySet()) {
			guilds.addAll(label.getGuildIds());
		}

		for (long guildId : guilds) {
			System.out.println("Unregistering Command");
			this.bot.getGuildById(guildId).updateCommands().complete();
			System.out.println("Command unregistered");
		}
	}
}