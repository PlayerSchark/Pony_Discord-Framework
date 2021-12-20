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
import io.schark.pony.core.feat.commands.command.info.*;
import io.schark.pony.core.feat.commands.executor.*;
import io.schark.pony.core.feat.commands.in.PonyChatLabel;
import io.schark.pony.core.feat.commands.registry.label.*;
import io.schark.pony.exception.CommandRegisterException;
import io.schark.pony.utils.PonyUtils;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
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
	private volatile BiMap<ChatRegistryEntry, PonyChatCommandExecutable> chatCommands = HashBiMap.create();
	private volatile BiMap<DiscordRegistryEntry, PonyDiscordCommandExecutable> discordCommands = HashBiMap.create();
	private volatile boolean isShutdown = false;
	private volatile boolean isShuttingDown;

	@SuppressWarnings("unused")
	private <E extends PonyChatCommandExecutable> void reflectExecutors() { //reflections access <- visibility
		new Thread(() -> {
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

	private <E extends PonyCommandExecutable, I extends PonyCommandInfo<E>> void registerCommands(Set<Class<E>> commands) {
		System.out.println("CommandRegistry: Awaiting JDA Ready");
		try {
			Pony.getInstance().awaitReady();
		}
		catch (InterruptedException e) {
			throw new CommandRegisterException(e);
		}
		for (Class<E> commandClass : commands) {
			if (Modifier.isAbstract(commandClass.getModifiers())) {
				continue;
			}
			try {
				System.out.println("Starting registering executor '" + commandClass.getSimpleName() + "'");
				Constructor<E> constructor = commandClass.getConstructor();
				RegistryEntry<E, I> registry = this.reflectAnnotations(constructor, commandClass);
				this.put(registry);

				new Thread(() -> this.registerDiscordCommand(registry)).start();

			}
			catch (NoSuchMethodException e) {
				throw new CommandRegisterException(e);
			}
		}
	}

	private <E extends PonyCommandExecutable, I extends PonyCommandInfo<E>> void registerDiscordCommand(RegistryEntry<E, I> entry) {
		if (entry instanceof DiscordRegistryEntry discordLabel) {
			System.out.println("DiscordCommands: Registering " + discordLabel.getRawLabel());
			E executor = entry.getCommandInfo().getExecutable();
			switch (entry) {
			case GuildRegistryEntry lbl -> this.registerGuildCommand(lbl, ((PonyGuildCommandExecutable) executor));
			case SlashRegistryEntry ignore -> this.registerSlashCommand(((PonySlashCommandExecutable) executor));
			default -> throw new IllegalStateException("Unexpected value: " + entry);
			}
		}
	}

	private <E extends PonyGuildCommandExecutable> void registerGuildCommand(GuildRegistryEntry label, E executor) {
		for (Long guildId : label.getGuildIds()) {
			this.registerGuildCommand(executor, guildId);
		}
	}

	private <E extends PonyGuildCommandExecutable> void registerGuildCommand(E executor, Long guildId) {
		this.registerDiscordCommand(executor, this.bot.getGuildById(guildId)::updateCommands);
	}

	private <E extends PonySlashCommandExecutable> void registerSlashCommand(E executor) {
		this.registerDiscordCommand(executor, this.bot.getJda()::updateCommands);
	}

	private <E extends PonyDiscordCommandExecutable> void registerDiscordCommand(final E executor, Supplier<CommandListUpdateAction> resolver) {
		CommandListUpdateAction action = resolver.get();
		this.addCommandData(executor, action);
		action.queue(x -> System.out.println("DiscordCommand: Executor '" + executor.getRawLabel() + "' done."));
		System.out.println("DiscordCommand: Executor '" + executor.getRawLabel() + "' queued.");
	}

	private <E extends PonyDiscordCommandExecutable> void addCommandData(E executor, CommandListUpdateAction action) {
		CommandData commandData = executor.getCommandData();
		OptionData single = executor.withSingleOptionData();
		if (single != null) {
			commandData.addOptions(single);
		}
		commandData.addOptions(executor.withOptionData());
		//noinspection ResultOfMethodCallIgnored
		action.addCommands(commandData);
	}

	private <E extends PonyCommandExecutable, I extends PonyCommandInfo<E>> void put(RegistryEntry<E, I> entry) {
		switch (entry) {
		case ChatRegistryEntry regEnt -> {
			this.chatCommands.put(regEnt, regEnt.getCommandInfo().getExecutable());
			System.out.println("Command " + regEnt.getRawLabel() + " registered to chat.");
		}
		case DiscordRegistryEntry regEnt -> {
			this.discordCommands.put(regEnt, regEnt.getCommandInfo().getExecutable());
			System.out.println("Command " + regEnt.getRawLabel() + " registered to guilds.");
		}
		default -> throw new IllegalStateException("Unexpected value: " + entry.getCommandInfo().getExecutable());
		}
	}

	private <E extends PonyCommandExecutable, I extends PonyCommandInfo<E>> RegistryEntry<E, I> reflectAnnotations(Constructor<E> constructor, Class<E> commandClass) {
		E command = this.executorInstance(commandClass);
		PonyCommandInfo<E> commandInfo = this.generateInfo(constructor, command);
		Set<String> aliases = this.assignAnnotation(constructor, Alias.class, anno -> Arrays.asList(anno.aliases()));

		return this.commandInstance(command, aliases, commandInfo);
	}

	private <E extends PonyCommandExecutable> PonyCommandInfo<E> generateInfo(Constructor<E> constructor, PonyCommandExecutable command) {
		//TODO: Many To Object
		Class<AllowedRoles> rolesClass = AllowedRoles.class;
		Class<AllowedUsers> usersClass = AllowedUsers.class;
		Class<AllowedGuilds> guildsClass = AllowedGuilds.class;
		Class<AllowedChannels> channelsClass = AllowedChannels.class;

		//TODO: Many To Object
		Set<Long> roles = this.assignAnnotation(constructor, rolesClass, anno -> this.idsFromAcessor(command, anno, AllowedRoles::accessor, a -> PonyUtils.toUpperLong(a.ids())));
		Set<Long> users = this.assignAnnotation(constructor, usersClass, anno -> this.idsFromAcessor(command, anno, AllowedUsers::accessor, a -> PonyUtils.toUpperLong(a.ids())));
		Set<Long> guilds = this.assignAnnotation(constructor, guildsClass, anno -> this.idsFromAcessor(command, anno, AllowedGuilds::accessor, a -> PonyUtils.toUpperLong(a.ids())));
		Set<Long> channels = this.assignAnnotation(constructor, channelsClass, anno -> this.idsFromAcessor(command, anno, AllowedChannels::accessor, a -> PonyUtils.toUpperLong(a.ids())));

		boolean botUsable = constructor.isAnnotationPresent(BotUsable.class);
		boolean sendTyping = constructor.isAnnotationPresent(SendTyping.class);
		boolean isCaseSensitive = constructor.isAnnotationPresent(CaseSensitive.class);
		boolean blacklisted = constructor.isAnnotationPresent(Blacklisted.class);

		//TODO: Many To Object
		PonyRunnable noRole = this.solveAnnotation(constructor, rolesClass, anno -> this.instanceFunction(anno.noAccessFunction()));
		PonyRunnable noUser = this.solveAnnotation(constructor, usersClass, anno -> this.instanceFunction(anno.noAccessFunction()));
		PonyRunnable noGuild = this.solveAnnotation(constructor, guildsClass, anno->this.instanceFunction(anno.noAccessFunction()));
		PonyRunnable noChannel = this.solveAnnotation(constructor, channelsClass, anno->this.instanceFunction(anno.noAccessFunction()));


		//TODO: Many To Object
		String noRoleMessage = this.solveAnnotation(constructor, rolesClass, AllowedRoles::noAccessMessage);
		String noUserMessage = this.solveAnnotation(constructor, usersClass, AllowedUsers::noAccessMessage);
		String noGuildMessage = this.solveAnnotation(constructor, guildsClass, AllowedGuilds::noAccessMessage);
		String noChannelMessage = this.solveAnnotation(constructor, channelsClass, AllowedChannels::noAccessMessage);

		PonyCommandInfo createdInfo;

		if (command instanceof PonyDiscordCommandExecutable discordExecutable) {
			boolean sendThinking = constructor.isAnnotationPresent(SendThinking.class);
			boolean isEphemeral = constructor.isAnnotationPresent(Ephemeral.class);
			if (discordExecutable instanceof PonyGuildCommandExecutable executable) { //isGuildCommand
				guilds = this.idsFromAcessor(command, executable, PonyGuildCommandExecutable::accessor, ex -> PonyUtils.toUpperLong(ex.guildIds()));
				createdInfo = new PonyGuildCommandInfo(executable, roles, users, guilds, channels,
								botUsable, isCaseSensitive, blacklisted, sendThinking, isEphemeral,
								noRole, noUser, noGuild, noChannel,
								noRoleMessage, noUserMessage, noGuildMessage, noChannelMessage);
			}
			else if (discordExecutable instanceof PonySlashCommandExecutable executable) { //isSlashCommand
				guilds = this.getBot().getGuildIds();
				createdInfo = new PonySlashCommandInfo(executable, roles, users, guilds, channels,
								botUsable, isCaseSensitive, blacklisted, sendThinking, isEphemeral,
								noRole, noUser, noGuild, noChannel,
								noRoleMessage, noUserMessage, noGuildMessage, noChannelMessage);
			}
			else {
				throw new IllegalStateException("Unexpected instance " + discordExecutable.getClass().getSimpleName());
			}
		}
		else if (command instanceof PonyChatCommandExecutable executable) {
			createdInfo = new PonyChatCommandInfo(executable, roles, users, guilds, channels,
							botUsable, sendTyping, isCaseSensitive, blacklisted,
							noRole, noUser, noGuild, noChannel,
							noRoleMessage, noUserMessage, noGuildMessage, noChannelMessage);
		}
		else {
			throw new IllegalStateException("Unexpected instance " + command.getClass().getSimpleName());
		}

		//noinspection unchecked
		return createdInfo;
	}

	private PonyRunnable instanceFunction(Class<? extends PonyRunnable> clazz) {
		try {
			if (PonyRunnable.class.equals(clazz)) {
				return () -> {
				};
			}
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

	private <E extends PonyCommandExecutable, I extends PonyCommandInfo<E>> RegistryEntry<E, I> commandInstance(E executable, Set<String> aliases, PonyCommandInfo<E> commandInfo) {
		String rawLabel = executable.getRawLabel();
		if (!commandInfo.isCaseSensitive()) {
			rawLabel = rawLabel.toLowerCase(Locale.ROOT);
		}

		return this.getRegistryEntry(aliases, commandInfo, rawLabel);
	}

	@NotNull
	@SuppressWarnings("unchecked")
	private <E extends PonyCommandExecutable, I extends PonyCommandInfo<E>> RegistryEntry<E, I> getRegistryEntry(Set<String> aliases, PonyCommandInfo<E> commandInfo, String rawLabel) {
		return (RegistryEntry<E, I>) switch (commandInfo) {
			case PonyGuildCommandInfo guildInfo -> new GuildRegistryEntry(rawLabel, guildInfo, guildInfo.getAllowedGuilds());
			case PonySlashCommandInfo slashInfo -> new SlashRegistryEntry(rawLabel, slashInfo);
			case PonyChatCommandInfo chatInfo -> new ChatRegistryEntry(rawLabel, aliases, chatInfo);
			case default -> throw new IllegalStateException("Unexpected instance " + commandInfo.getClass().getSimpleName());
		};
	}

	private <E extends PonyCommandExecutable> E executorInstance(Class<E> commandClass) {
		try {
			return commandClass.getConstructor().newInstance();
		}
		catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new CommandRegisterException(e);
		}
	}

	public boolean isChatCommandRegistered(String label) {
		return Boolean.TRUE.equals(this.forChatCommands(reg -> reg.matches(label), x -> Boolean.TRUE));
	}

	@Nullable public PonyChatCommandExecutable getChatCommand(PonyChatLabel label) {
		return this.getChatCommand(label.getContent());
	}

	@Nullable public PonyChatCommandExecutable getChatCommand(String label) {
		return this.forChatCommands(lbl -> lbl.matches(label), this::getChatCommand);
	}

	@Nullable private PonyChatCommandExecutable getPonyChatCommandExecutor(ChatRegistryEntry label) {
		return this.forChatCommands(reg -> reg.equals(label), this.chatCommands::get);
	}

	@Nullable public PonyChatCommandExecutable getChatCommand(ChatRegistryEntry label) {
		return this.getPonyChatCommandExecutor(label);
	}

	@Nullable private <R> R forChatCommands(Function<ChatRegistryEntry, Boolean> matchIf, Function<ChatRegistryEntry, R> result) {
		return this.forCommands(this.chatCommands, matchIf, result);
	}

	@Nullable public PonyDiscordCommandExecutable getDiscordCommand(String label) {
		return this.forDiscordCommands(lbl -> lbl.matches(label), this::getDiscordCommand);
	}

	@Nullable private PonyDiscordCommandExecutable getPonyDiscordCommandExecutor(DiscordRegistryEntry label) {
		return this.forDiscordCommands(reg -> reg.equals(label), this.discordCommands::get);
	}

	@Nullable public PonyDiscordCommandExecutable getDiscordCommand(DiscordRegistryEntry label) {
		return this.getPonyDiscordCommandExecutor(label);
	}

	@Nullable private <R> R forDiscordCommands(Function<DiscordRegistryEntry, Boolean> matchIf, Function<DiscordRegistryEntry, R> result) {
		return this.forCommands(this.discordCommands, matchIf, result);
	}

	@NotNull private <L extends DiscordRegistryEntry> Map<L, PonyDiscordCommandExecutable> getRegistryLabel(Class<L> clazz) {
		Map<L, PonyDiscordCommandExecutable> commands = new HashMap<>();
		for (Map.Entry<DiscordRegistryEntry, PonyDiscordCommandExecutable> entry : this.discordCommands.entrySet()) {
			DiscordRegistryEntry key = entry.getKey();
			if (key.getClass().isAssignableFrom(clazz)) {
				commands.put(clazz.cast(key), entry.getValue());
			}
		}
		return commands;
	}

	@Nullable private <L extends RegistryEntry, E extends PonyCommandExecutable, R> R forCommands(Map<L, E> map, Function<L, Boolean> matchIf, Function<L, R> result) {
		for (L registryLabel : map.keySet()) {
			if (matchIf.apply(registryLabel)) {
				return result.apply(registryLabel);
			}
		}
		return null;
	}

	public ChatRegistryEntry getLabel(PonyChatLabel label) {
		return this.forChatCommands(req -> req.matches(label.getContent()), reg -> reg);
	}

	public PonyCommandInfo<PonyChatCommandExecutable> getChatCommandInfo(PonyChatCommandExecutor executor) {
		return this.getCommandInfo(executor, this.chatCommands).getCommandInfo();
	}

	public PonyDiscordCommandInfo getDiscordCommandInfo(PonyDiscordCommandExecutor executor) {
		return this.getCommandInfo(executor, this.discordCommands).getCommandInfo();
	}

	private <R extends RegistryEntry, E extends PonyCommandExecutable> R getCommandInfo(E executor, BiMap<R, E> commands) {
		return commands.inverse().get(executor);
	}

	public synchronized void shutdown() {
		if (this.isShuttingDown || this.isShutdown) {
			System.out.println("Registry is already stopped or is currently stopping.");
			return;
		}
		this.isShuttingDown = true;
		if (Pony.getInstance().getConfig().unregisterCommands()) {
			System.out.println("Shutting down Commands");
			for (DiscordRegistryEntry label : this.discordCommands.keySet()) {
				System.out.println("Unregistering Command");
				if (label instanceof GuildRegistryEntry guildLabel) {
					Set<Long> guildIds = guildLabel.getGuildIds();
					this.unregisterCommands(guildIds);
				}
				else if (label instanceof SlashRegistryEntry) {
					Set<Long> guildIds = this.getBot().getGuildIds();
					this.unregisterCommands(guildIds);
				}
				else {
					throw new IllegalStateException("Unexcpeted label type " + label.getClass().getSimpleName());
				}
				System.out.println("Command unregistered");
			}
		}
		this.isShutdown = true;
	}

	private void unregisterCommands(Set<Long> guildIds) {
		for (Long guildId : guildIds) {
			Guild guildById = this.bot.getGuildById(guildId);
			guildById.updateCommands().complete();
		}
	}
}