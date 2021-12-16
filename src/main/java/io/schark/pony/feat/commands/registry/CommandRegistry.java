package io.schark.pony.feat.commands.registry;

import com.google.common.collect.*;
import io.schark.pony.feat.commands.annotation.impl.PonyAccessor;
import io.schark.pony.feat.commands.PonyCommandComponent;
import io.schark.pony.feat.commands.PonyChatCommandExecutor;
import io.schark.pony.feat.commands.annotation.Alias;
import io.schark.pony.feat.commands.annotation.impl.PonyFunction;
import io.schark.pony.feat.commands.annotation.options.BotUsable;
import io.schark.pony.feat.commands.annotation.access.AllowedChannels;
import io.schark.pony.feat.commands.annotation.access.AllowedGuilds;
import io.schark.pony.feat.commands.annotation.access.AllowedRoles;
import io.schark.pony.feat.commands.annotation.options.CaseSensitive;
import io.schark.pony.feat.commands.annotation.access.AllowedUsers;
import io.schark.pony.feat.commands.annotation.options.SendTyping;
import io.schark.pony.feat.commands.command.CommandInfo;
import io.schark.pony.feat.commands.in.PonyLabel;
import io.schark.pony.exception.CommandRegisterException;
import io.schark.pony.utils.PonyUtils;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;

/**
 * @author Player_Schark
 */
public class CommandRegistry {

	private BiMap<RegistryLabel, PonyChatCommandExecutor> commands = HashBiMap.create();

	/**
	 * searches for annotations in the constructor of a {@link PonyChatCommandExecutor}
	 *
	 * @param command executor that should be registered
	 */
	public void register(PonyChatCommandExecutor command) {
		this.reflectAnnotations(command);
	}

	private void reflectAnnotations(PonyChatCommandExecutor command) {
		try {
			Constructor<? extends PonyChatCommandExecutor> constructor = command.getClass().getConstructor(String.class, PonyCommandComponent.class);

			CommandInfo access = this.generateInfo(constructor, command);
			Set<String> aliases = this.assignAnnotation(constructor, Alias.class, anno->Arrays.asList(anno.aliases()));

			this.registerCommand(command, aliases, access);
		}
		catch (NoSuchMethodException e) {
			throw new CommandRegisterException();
		}
	}

	private CommandInfo generateInfo(Constructor<? extends PonyChatCommandExecutor> constructor, PonyChatCommandExecutor command) {
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

	private <T extends Annotation> Set<Long> idsFromAcessor(PonyChatCommandExecutor command, T anno,
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

	private Set<Long> solveAccessorIds(PonyChatCommandExecutor command, Class<? extends PonyAccessor> accessorClass) {
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
	private <E extends PonyChatCommandExecutor, T extends Annotation, V> Set<V> assignAnnotation(Constructor<E> constructor,
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

	private void registerCommand(PonyChatCommandExecutor command, Set<String> aliases, CommandInfo commandInfo) {
		String rawLabel = command.getRawLabel();
		if (!commandInfo.isCaseSensitive()) {
			rawLabel = rawLabel.toLowerCase(Locale.ROOT);
		}
		RegistryLabel label = new RegistryLabel(rawLabel, aliases, commandInfo);
		this.commands.put(label, command);
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
		return this.forCommands(reg->reg.equals(label), this.commands::get);
	}

	@Nullable  private <T> T forCommands(Function<RegistryLabel, Boolean> matchIf, Function<RegistryLabel, T> result) {
		for (RegistryLabel registryLabel : this.commands.keySet()) {
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
		RegistryLabel label = this.commands.inverse().get(executor);
		return label.getCommandInfo();
	}
}