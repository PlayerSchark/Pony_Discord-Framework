package io.schark.pony.core.feat.commands.registry;

import com.google.common.collect.Sets;
import io.schark.pony.Pony;
import io.schark.pony.core.PonyBot;
import io.schark.pony.core.feat.commands.annotation.Alias;
import io.schark.pony.core.feat.commands.in.PonyChatLabel;
import io.schark.pony.exception.CommandRegisterException;
import io.schark.pony.utils.PonyMulti;
import io.schark.pony.utils.PonyUtils;
import io.schark.pony.core.feat.commands.annotation.access.AllowedChannels;
import io.schark.pony.core.feat.commands.annotation.access.AllowedGuilds;
import io.schark.pony.core.feat.commands.annotation.access.AllowedRoles;
import io.schark.pony.core.feat.commands.annotation.access.AllowedUsers;
import io.schark.pony.core.feat.commands.annotation.impl.PonyAccessor;
import io.schark.pony.core.feat.commands.annotation.impl.PonyRunnable;
import io.schark.pony.core.feat.commands.annotation.options.*;
import io.schark.pony.core.feat.commands.executor.PonyCommandExecutable;
import io.schark.pony.core.feat.commands.executor.PonyCommandExecutor;
import io.schark.pony.core.feat.commands.executor.input.*;
import io.schark.pony.core.feat.commands.registry.info.*;
import io.schark.pony.core.feat.commands.registry.label.*;
import io.schark.pony.core.feat.commands.registry.wrapper.PonyAccessWrapper;
import io.schark.pony.core.feat.commands.registry.wrapper.PonyAnnotationWrapper;
import io.schark.pony.core.feat.commands.registry.wrapper.WrapperType;
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

	private static final Class<PonyAccessor> PONY_ACCESSOR_CLASS = PonyAccessor.class;
	private volatile PonyBot bot;
	private volatile boolean noCommands;
	private volatile PonyMulti<PonyRegistryEntry, PonyCommandExecutable> commands = new PonyMulti<>();
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
				PonyRegistryEntry<E, I> registry = this.reflectAnnotations(constructor, commandClass);
				this.put(registry);

				new Thread(() -> this.registerDiscordCommand(registry)).start();

			}
			catch (NoSuchMethodException e) {
				throw new CommandRegisterException(e);
			}
		}
	}

	private <E extends PonyCommandExecutable, I extends PonyCommandInfo<E>> void registerDiscordCommand(PonyRegistryEntry<E, I> entry) {
		if (entry instanceof PonyDiscordRegistryEntry discordLabel) {
			System.out.println("DiscordCommands: Registering " + discordLabel.getRawLabel());
			E executor = entry.getCommandInfo().getExecutable();
			switch (entry) {
			case PonyGuildRegistryEntry lbl -> this.registerGuildCommand(lbl, ((PonyGuildCommandExecutable) executor));
			case PonySlashRegistryEntry ignore -> this.registerSlashCommand(((PonySlashCommandExecutable) executor));
			default -> throw new IllegalStateException("Unexpected value: " + entry);
			}
		}
	}

	private <E extends PonyGuildCommandExecutable> void registerGuildCommand(PonyGuildRegistryEntry label, E executor) {
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

	private <E extends PonyCommandExecutable, I extends PonyCommandInfo<E>> void put(PonyRegistryEntry<E, I> entry) {
		this.commands.put(entry, entry.getCommandInfo().getExecutable());
	}

	private <E extends PonyCommandExecutable, I extends PonyCommandInfo<E>> PonyRegistryEntry<E, I> reflectAnnotations(Constructor<E> constructor, Class<E> commandClass) {
		E command = this.executorInstance(commandClass);
		PonyCommandInfo<E> commandInfo = this.generateInfo(constructor, command);
		Set<String> aliases = this.assignAnnotation(constructor, Alias.class, anno -> Arrays.asList(anno.aliases()));

		return this.commandInstance(command, aliases, commandInfo);
	}

	private <A extends Annotation, E extends PonyCommandExecutable> PonyAccessWrapper<A> getWrapper(Class<A> annoClass, Constructor<E> constructor, PonyCommandExecutable command) {
		Set<Long> accessIds = this.assignAnnotation(constructor, annoClass, anno -> this.accessorIdsFromAnno(command, anno));
		PonyRunnable noAccess = this.solveAnnotation(constructor, annoClass, anno -> this.instanceFunction(PonyUtils.invokeClass(anno, "noAccessFunction", PonyRunnable.class)));
		String noAccessMessage = this.solveAnnotation(constructor, annoClass, a -> PonyUtils.invoke(a, "noAccessMessage", String.class));

		return new PonyAccessWrapper<>(annoClass, accessIds, noAccess, noAccessMessage);
	}

	@NotNull private <A extends Annotation> Set<Long> accessorIdsFromAnno(PonyCommandExecutable command, A anno) {
		Class<? extends PonyAccessor> accessor = PonyUtils.invokeClass(anno, "accessor", PonyCommandRegistry.PONY_ACCESSOR_CLASS);
		long[] ids = PonyUtils.invoke(anno, "ids", long[].class);
		return this.idsFromAcessor(command, accessor, ids);
	}

	private <E extends PonyCommandExecutable> PonyCommandInfo<E> generateInfo(Constructor<E> constructor, PonyCommandExecutable command) {
		//TODO: Many To Object
		PonyAccessWrapper<AllowedRoles> rolesWrapper = this.getWrapper(AllowedRoles.class, constructor, command);
		PonyAccessWrapper<AllowedUsers> usersWrapper = this.getWrapper(AllowedUsers.class, constructor, command);
		PonyAccessWrapper<AllowedGuilds> guildsWrapper = this.getWrapper(AllowedGuilds.class, constructor, command);
		PonyAccessWrapper<AllowedChannels> channelsWrapper = this.getWrapper(AllowedChannels.class, constructor, command);

		boolean botUsable = constructor.isAnnotationPresent(BotUsable.class);
		boolean sendTyping = constructor.isAnnotationPresent(SendTyping.class);
		boolean isCaseSensitive = constructor.isAnnotationPresent(CaseSensitive.class);
		boolean blacklisted = constructor.isAnnotationPresent(Blacklisted.class);

		PonyCommandInfo createdInfo;
		PonyAnnotationWrapper wrapper = new PonyAnnotationWrapper(rolesWrapper, usersWrapper, guildsWrapper, channelsWrapper);

		if (command instanceof PonyDiscordCommandExecutable discordExecutable) {
			createdInfo = this.getPonyDiscordCommandInfo(constructor, command, guildsWrapper, botUsable, isCaseSensitive, blacklisted, wrapper, discordExecutable);
		}
		else if (command instanceof PonyChatCommandExecutable executable) {
			createdInfo = new PonyChatCommandInfo(executable, wrapper, botUsable, sendTyping, isCaseSensitive, blacklisted);
		}
		else {
			throw new IllegalStateException("Unexpected instance " + command.getClass().getName());
		}

		//noinspection unchecked
		return createdInfo;
	}

	@NotNull private <E extends PonyCommandExecutable> PonyCommandInfo getPonyDiscordCommandInfo(Constructor<E> constructor, PonyCommandExecutable command,
					PonyAccessWrapper<AllowedGuilds> guildsWrapper,
					boolean botUsable, boolean isCaseSensitive, boolean blacklisted, PonyAnnotationWrapper wrapper, PonyDiscordCommandExecutable discordExecutable) {
		PonyCommandInfo createdInfo;
		boolean sendThinking = constructor.isAnnotationPresent(SendThinking.class);
		boolean isEphemeral = constructor.isAnnotationPresent(Ephemeral.class);
		if (discordExecutable instanceof PonyGuildCommandExecutable executable) { //isGuildCommand
			Set<Long> guilds = this.idsFromAcessor(command, executable.accessor(), executable.guildIds());
			this.changeGuildAccess(guildsWrapper, wrapper, guilds);
			createdInfo = new PonyGuildCommandInfo(executable, wrapper, botUsable, isCaseSensitive, blacklisted, sendThinking, isEphemeral);
		}
		else if (discordExecutable instanceof PonySlashCommandExecutable executable) { //isSlashCommand
			this.changeGuildAccess(guildsWrapper, wrapper, this.getBot().getGuildIds());
			createdInfo = new PonySlashCommandInfo(executable, wrapper, botUsable, isCaseSensitive, blacklisted, sendThinking, isEphemeral);
		}
		else {
			throw new IllegalStateException("Unexpected instance " + discordExecutable.getClass().getSimpleName());
		}
		return createdInfo;
	}

	private void changeGuildAccess(PonyAccessWrapper<AllowedGuilds> guildsWrapper, PonyAnnotationWrapper wrapper, Set<Long> ids) {
		guildsWrapper = new PonyAccessWrapper<>(guildsWrapper.annoClass(), ids, guildsWrapper.noAccess(), guildsWrapper.noAccessMessage());
		wrapper.setAccessWrapper(WrapperType.GUILD, guildsWrapper);
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

	private Set<Long> idsFromAcessor(PonyCommandExecutable command,
					Class<? extends PonyAccessor> accessorClass, long[] ids) {
		Set<Long> accessorIds = new HashSet<>();

		if (!PonyCommandRegistry.PONY_ACCESSOR_CLASS.equals(accessorClass)) {
			accessorIds.addAll(this.solveAccessorIds(command, accessorClass));
		}

		accessorIds.addAll(PonyUtils.toUpperLong(ids));
		return accessorIds;
	}

	private Set<Long> solveAccessorIds(PonyCommandExecutable command, Class<? extends PonyAccessor> accessorClass) {
		try {
			PonyAccessor accessor = accessorClass.getConstructor().newInstance();
			Long[] accessorIds = accessor.getIds(command);
			return Sets.newHashSet(accessorIds);
		}
		catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			System.out.println("Class: " + accessorClass.getName());
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

	private <E extends PonyCommandExecutable, I extends PonyCommandInfo<E>> PonyRegistryEntry<E, I> commandInstance(E executable, Set<String> aliases, PonyCommandInfo<E> commandInfo) {
		String rawLabel = executable.getRawLabel();
		if (!commandInfo.isCaseSensitive()) {
			rawLabel = rawLabel.toLowerCase(Locale.ROOT);
		}

		return this.getRegistryEntry(aliases, commandInfo, rawLabel);
	}

	@NotNull
	@SuppressWarnings("unchecked")
	private <E extends PonyCommandExecutable, I extends PonyCommandInfo<E>> PonyRegistryEntry<E, I> getRegistryEntry(Set<String> aliases, PonyCommandInfo<E> commandInfo, String rawLabel) {
		return (PonyRegistryEntry<E, I>) switch (commandInfo) {
			case PonyGuildCommandInfo guildInfo -> new PonyGuildRegistryEntry(rawLabel, guildInfo, guildInfo.getAllowedGuilds());
			case PonySlashCommandInfo slashInfo -> new PonySlashRegistryEntry(rawLabel, slashInfo);
			case PonyChatCommandInfo chatInfo -> new PonyChatRegistryEntry(rawLabel, aliases, chatInfo);
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
		for (PonyRegistryEntry entry : this.commands.getAllLeft()) {
			if (entry.matches(label)) {
				return true;
			}
		}
		return false;
	}

	@Nullable public PonyChatCommandExecutable getChatCommand(PonyChatLabel label) {
		return this.getChatCommand(label.getContent());
	}

	@Nullable public PonyChatCommandExecutable getChatCommand(String label) {
		return this.getPonyChatCommandExecutor(this.getRegistryEntry(label, PonyRegistryType.CHAT));
	}

	@Nullable private PonyChatCommandExecutable getPonyChatCommandExecutor(PonyChatRegistryEntry label) {
		return this.commands.getRightValue(label, PonyChatCommandExecutable.class);
	}

	@Nullable public PonyChatCommandExecutable getChatCommand(PonyChatRegistryEntry label) {
		return this.getPonyChatCommandExecutor(label);
	}

	@Nullable public PonyDiscordCommandExecutable getDiscordCommand(String label) {
		return this.getPonyDiscordCommandExecutor(this.getRegistryEntry(label, PonyRegistryType.DISCORD));
	}

	@Nullable private <R extends PonyRegistryEntry> R getRegistryEntry(String label, PonyRegistryType<R> type) {
		for (PonyRegistryEntry entry : this.commands.getAllLeft()) {
			if (type.isInstance(entry) && entry.matches(label)) {
				return type.cast(entry);
			}
		}
		return null;
	}

	@Nullable private PonyDiscordCommandExecutable getPonyDiscordCommandExecutor(PonyDiscordRegistryEntry label) {
		return this.commands.getRightValue(label, PonyDiscordCommandExecutable.class);
	}

	@Nullable public PonyDiscordCommandExecutable getDiscordCommand(PonyDiscordRegistryEntry label) {
		return this.getPonyDiscordCommandExecutor(label);
	}

	public PonyChatCommandInfo getChatCommandInfo(PonyChatCommandExecutor executor) {
		return this.commands.getLeftValue(executor, PonyRegistryType.CHAT.typeClass()).getCommandInfo();
	}

	public PonyDiscordCommandInfo getDiscordCommandInfo(PonyDiscordCommandExecutor executor) {
		return this.commands.getLeftValue(executor, PonyRegistryType.DISCORD.typeClass()).getCommandInfo();
	}

	public synchronized void shutdown() {
		if (this.isShuttingDown || this.isShutdown) {
			System.out.println("Registry is already stopped or is currently stopping.");
			return;
		}
		this.isShuttingDown = true;
		if (Pony.getInstance().getConfig().unregisterCommands()) {
			System.out.println("Shutting down Commands");
			for (PonyRegistryEntry entry : this.commands.getAllLeft()) {
				if (entry instanceof PonyDiscordRegistryEntry<?, ?> label) {
					System.out.println("Unregistering Command");
					if (label instanceof PonyGuildRegistryEntry guildLabel) {
						Set<Long> guildIds = guildLabel.getGuildIds();
						this.unregisterCommands(guildIds);
					}
					else if (label instanceof PonySlashRegistryEntry) {
						Set<Long> guildIds = this.getBot().getGuildIds();
						this.unregisterCommands(guildIds);
					}
					else {
						throw new IllegalStateException("Unexcpeted label type " + label.getClass().getSimpleName());
					}
					System.out.println("Command unregistered");
				}
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