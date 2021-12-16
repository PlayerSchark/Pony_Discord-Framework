package io.schark.pony.core.feat.commands.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import io.schark.pony.Pony;
import io.schark.pony.core.feat.commands.annotation.Alias;
import io.schark.pony.core.feat.commands.annotation.access.AllowedChannels;
import io.schark.pony.core.feat.commands.annotation.access.AllowedGuilds;
import io.schark.pony.core.feat.commands.annotation.access.AllowedRoles;
import io.schark.pony.core.feat.commands.annotation.access.AllowedUsers;
import io.schark.pony.core.feat.commands.annotation.impl.PonyAccessor;
import io.schark.pony.core.feat.commands.annotation.impl.PonyFunction;
import io.schark.pony.core.feat.commands.annotation.options.BotUsable;
import io.schark.pony.core.feat.commands.annotation.options.CaseSensitive;
import io.schark.pony.core.feat.commands.annotation.options.SendTyping;
import io.schark.pony.core.feat.commands.command.CommandInfo;
import io.schark.pony.core.feat.commands.comp.PonyCommandComponent;
import io.schark.pony.core.feat.commands.executor.PonyChatCommandExecutor;
import io.schark.pony.core.feat.commands.executor.PonyCommandExecutor;
import io.schark.pony.core.feat.commands.in.PonyLabel;
import io.schark.pony.core.feat.commands.slash.PonySlashCommandExecutor;
import io.schark.pony.exception.CommandRegisterException;
import io.schark.pony.utils.PonyUtils;
import lombok.Getter;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.internal.utils.tuple.ImmutablePair;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;

/**
 * @author Player_Schark
 */
@Getter
public class PonyCommandRegistry {

	private boolean noCommands;
	private BiMap<RegistryLabel, PonyChatCommandExecutor> chatCommands = HashBiMap.create();
	private BiMap<RegistryLabel, PonySlashCommandExecutor> slashCommands = HashBiMap.create();

	private <E extends PonyChatCommandExecutor> void reflectCommands() {
		String commandPackage = Pony.getInstance().getConfig().getCommandPackage();
		if (commandPackage == null) {
			this.noCommands = true;
			return;
		}
		Set<Class<E>> commands = this.getSubTypes(commandPackage);
		this.registerCommands(commands);
	}

	@SuppressWarnings("unchecked")
	private <E extends PonyChatCommandExecutor> Set<Class<E>> getSubTypes(String commandPackage) {
		Reflections reflections = new Reflections(commandPackage);
		Set<Class<? extends PonyCommandExecutor>> subTypes = reflections.getSubTypesOf(PonyCommandExecutor.class);

		Set<Class<E>> types = new HashSet<>();
		for (Class<? extends PonyCommandExecutor> subType : subTypes) {
			types.add((Class<E>) subType);
		}

		return types;
	}

	private <E extends PonyCommandExecutor> void registerCommands(Set<Class<E>> commands) {
		for (Class<E> commandClass : commands) {
			try {
				Constructor<E> constructor = commandClass.getConstructor(String.class, PonyCommandComponent.class);
				Pair<RegistryLabel, E> registry = this.reflectAnnotations(constructor, commandClass);
				this.put(registry);
			}
			catch (NoSuchMethodException e) {
				throw new CommandRegisterException();
			}
		}
	}

	private <E extends PonyCommandExecutor> void put(Pair<RegistryLabel,E> registry) {
		PonyCommandExecutor command = registry.getRight();
		RegistryLabel label = registry.getLeft();

		switch (command) {
			case PonyChatCommandExecutor executor -> this.chatCommands.put(label, executor);
			case PonySlashCommandExecutor executor -> this.slashCommands.put(label, executor);
			default -> throw new IllegalStateException("Unexpected value: " + command);
		}
	}

	private <E extends PonyCommandExecutor> Pair<RegistryLabel, E> reflectAnnotations(Constructor<E> constructor, Class<E> commandClass) {
		E command = this.executorInstance(commandClass);
		CommandInfo access = this.generateInfo(constructor, command);
		Set<String> aliases = this.assignAnnotation(constructor, Alias.class, anno->Arrays.asList(anno.aliases()));

		return this.commandInstance(command, aliases, access);
	}

	private CommandInfo generateInfo(Constructor<? extends PonyCommandExecutor> constructor, PonyCommandExecutor command) {
		Class<AllowedRoles> rolesClass = AllowedRoles.class;
		Class<AllowedUsers> usersClass = AllowedUsers.class;
		Class<AllowedGuilds> guildsClass = AllowedGuilds.class;
		Class<AllowedChannels> channelsClass = AllowedChannels.class;

		Set<Long> roles = this.assignAnnotation(constructor, rolesClass, anno->this.idsFromAcessor(command, anno, AllowedRoles::accessor, a->PonyUtils.toUpperLong(a.ids())));
		Set<Long> users = this.assignAnnotation(constructor, usersClass, anno->this.idsFromAcessor(command, anno, AllowedUsers::accessor, a->PonyUtils.toUpperLong(a.ids())));
		Set<Long> guilds = this.assignAnnotation(constructor, guildsClass, anno->this.idsFromAcessor(command, anno, AllowedGuilds::accessor, a->PonyUtils.toUpperLong(a.ids())));
		Set<Long> channels = this.assignAnnotation(constructor, channelsClass, anno->this.idsFromAcessor(command, anno, AllowedChannels::accessor, a->PonyUtils.toUpperLong(a.ids())));

		boolean botUsable = constructor.isAnnotationPresent(BotUsable.class);
		boolean sendTyping = constructor.isAnnotationPresent(SendTyping.class);
		boolean isCaseSensitive = constructor.isAnnotationPresent(CaseSensitive.class);

		PonyFunction noRole = this.solveAnnotation(constructor, rolesClass, anno->this.function(anno.noAccessFunction()));
		PonyFunction noUser = this.solveAnnotation(constructor, usersClass, anno->this.function(anno.noAccessFunction()));
		PonyFunction noGuild = this.solveAnnotation(constructor, guildsClass, anno->this.function(anno.noAccessFunction()));
		PonyFunction noChannel = this.solveAnnotation(constructor, channelsClass, anno->this.function(anno.noAccessFunction()));

		return new CommandInfo(roles, users, guilds, channels, botUsable, sendTyping, isCaseSensitive, noRole, noUser, noGuild, noChannel);
	}

	private PonyFunction function(Class<? extends PonyFunction> clazz) {
		try {
			return clazz.getConstructor().newInstance();
		}
		catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			e.printStackTrace();
			throw new CommandRegisterException();
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
			throw new CommandRegisterException();
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
	private <E extends PonyCommandExecutor, T extends Annotation, V> Set<V> assignAnnotation(Constructor<E> constructor,
																							Class<T> annotation,
																							Function<T, Collection<V>> getter) {
		Set<V> result = new HashSet<>();
		boolean isPresent = constructor.isAnnotationPresent(annotation);
		if (isPresent) {
			result = new HashSet<>(this.solveAnnotation(constructor, annotation, getter));
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
	private <E, T extends Annotation, V> V solveAnnotation(Constructor<E> constructor,
																Class<T> annotation,
																Function<T, V> getter) {
		T anno = constructor.getAnnotation(annotation);
		return getter.apply(anno);
	}

	private <E extends PonyCommandExecutor> Pair<RegistryLabel, E> commandInstance(E command, Set<String> aliases, CommandInfo commandInfo) {
		String rawLabel = command.getRawLabel();
		if (!commandInfo.isCaseSensitive()) {
			rawLabel = rawLabel.toLowerCase(Locale.ROOT);
		}
		RegistryLabel label = new RegistryLabel(rawLabel, aliases, commandInfo);
		return new ImmutablePair<>(label, command);
	}

	private <E extends PonyCommandExecutor> E executorInstance(Class<E> commandClass) {
		try {
			return commandClass.getConstructor().newInstance();
		}
		catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new CommandRegisterException();
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
		return this.forCommands(reg->reg.equals(label), this.chatCommands::get);
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

	public boolean hasAccess(Message message, PonyChatCommandExecutor executor) {
		CommandInfo access = this.getcommandInfo(executor);
		boolean isPublic = executor.isGuildCommand();

		User author = message.getAuthor();
		if (author.isBot() && !access.isBotUsable()) {
			return false;
		}

		MessageChannel channel = message.getChannel();
		if (isPublic) {
			//if channel is no guild but command is a guild command we need to reject - ALWAYS
			if (channel instanceof GuildChannel guildChannel) {
				//guild check
				if (!access.getAllowedGuilds().contains(guildChannel.getGuild().getIdLong())) {
					return false;
				}

				//role check
				Member member = message.getMember();
				assert member != null;
				if (this.hasAccess(access.getAllowedRoles(), member)) {
					return true;
				}
			}
			else {
				return false;
			}
		}

		//user check
		if (access.getAllowedUsers().contains(author.getIdLong())) {
			return true;
		}

		//channels
		return access.getAllowedChannels().contains(channel.getIdLong());
	}

	private boolean hasAccess(Set<Long> roles, Member member) {
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
}