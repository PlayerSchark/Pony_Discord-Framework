package io.schark.pony.core.feat.commands.registry.info;

import io.schark.pony.core.feat.commands.annotation.impl.PonyRunnable;
import io.schark.pony.core.feat.commands.executor.PonyCommandExecutable;
import io.schark.pony.core.feat.commands.executor.input.PonyChatCommandExecutor;
import io.schark.pony.core.feat.commands.registry.wrapper.PonyAnnotationWrapper;
import io.schark.pony.core.feat.commands.registry.wrapper.WrapperType;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Player_Schark
 */
@RequiredArgsConstructor
public abstract class PonyCommandInfo<E extends PonyCommandExecutable> {

	private final E executable;
	private final PonyAnnotationWrapper wrapper;
	private final boolean botUsable;
	private final boolean sendTyping;
	private final boolean caseSensitive;
	private final boolean blacklisted;

	protected <Ev extends Event> void runNoAccess(String noAccessMessage, Function<String, RestAction> action, PonyRunnable runnable) {
		if (runnable != null) {
			runnable.run();
		}
		if (noAccessMessage != null && !noAccessMessage.isEmpty()) {
			action.apply(noAccessMessage).queue();
		}
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	boolean hasAccess(Function<String, RestAction> noAccessFunction, User author, Member member,
					MessageChannel channel) {
		boolean blacklisted = this.isBlacklisted();
		if (author.isBot() && blacklisted == this.isBotUsable()) {
			return false;
		}

		E executable = this.getExecutable();
		boolean isPublic = executable instanceof PonyChatCommandExecutor executor && executor.isGuildCommand();
		boolean asMember = false;

		if (isPublic) {
			//if channel is no guild but command is a guild command we need to reject - ALWAYS
			if (member != null) {
				//guild check
				Set<Long> allowedGuilds = this.getAllowedGuilds();
				boolean guildAccess = !allowedGuilds.isEmpty() && !allowedGuilds.contains(member.getGuild().getIdLong());
				if (blacklisted != guildAccess) {
					this.runNoAccess(this.getNoGuildMessage(), noAccessFunction, this.getNoGuild());
					return false;
				}

				//role check
				if (blacklisted != this.hasAccess(this.getAllowedRoles(), member)) {
					asMember = true;
				}
				else {
					this.runNoAccess(this.getNoRoleMessage(), noAccessFunction, this.getNoRole());
					//asMember = false <- variable already assigned;
				}
			}
			else {
				return false;
			}
		}
		else {
			asMember = true;
		}

		//user check
		boolean asUser = this.hasUserAccess(noAccessFunction, this, author, asMember);

		//channels
		boolean asChannel = this.hasChannelAccess(noAccessFunction, this, channel, asUser);
		return asMember && asChannel && asUser;
	}

	private boolean hasChannelAccess(Function<String, RestAction> noAccessFunction, PonyCommandInfo<E> commandInfo, MessageChannel channel, boolean runNoAccess) {
		return this.hasIdAccess(noAccessFunction, channel.getIdLong(), commandInfo.getAllowedChannels(), commandInfo.getNoChannel(), commandInfo.getNoChannelMessage(), runNoAccess);
	}

	private boolean hasUserAccess(Function<String, RestAction> noAccessFunction, PonyCommandInfo<E> commandInfo, User author, boolean runNoAccess) {
		return this.hasIdAccess(noAccessFunction, author.getIdLong(), commandInfo.getAllowedUsers(), commandInfo.getNoUser(), commandInfo.getNoUserMessage(), runNoAccess);
	}

	private boolean hasIdAccess(Function<String, RestAction> noAccessFunction, Long id, Set<Long> allowedUsers,
					@Nullable PonyRunnable ponyRunnable, @Nullable String message, boolean runNoAccess) {
		boolean as = allowedUsers.isEmpty() || allowedUsers.contains(id);
		as = this.blacklisted != as;

		if (!as && runNoAccess) {
			this.runNoAccess(message, noAccessFunction, ponyRunnable);
		}

		return as;
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

	public E getExecutable() {
		return this.executable;
	}

	public Set<Long> getAllowedRoles() {
		return this.getAllowedIds(WrapperType.ROLE);
	}

	public Set<Long> getAllowedUsers() {
		return this.getAllowedIds(WrapperType.USER);
	}

	public Set<Long> getAllowedGuilds() {
		return this.getAllowedIds(WrapperType.GUILD);
	}

	public Set<Long> getAllowedChannels() {
		return this.getAllowedIds(WrapperType.CHANNEL);
	}

	private <A extends Annotation> Set<Long> getAllowedIds(WrapperType<A> type) {
		return this.wrapper.getAccessWrapper(type).accessIds();
	}

	public boolean isBotUsable() {
		return this.botUsable;
	}

	public boolean isSendTyping() {
		return this.sendTyping;
	}

	public boolean isCaseSensitive() {
		return this.caseSensitive;
	}

	public boolean isBlacklisted() {
		return this.blacklisted;
	}

	public PonyRunnable getNoRole() {
		return this.getNoRunnable(WrapperType.ROLE);
	}

	public PonyRunnable getNoUser() {
		return this.getNoRunnable(WrapperType.USER);
	}

	public PonyRunnable getNoGuild() {
		return this.getNoRunnable(WrapperType.GUILD);
	}

	public PonyRunnable getNoChannel() {
		return this.getNoRunnable(WrapperType.CHANNEL);
	}

	private <A extends Annotation> PonyRunnable getNoRunnable(WrapperType<A> type) {
		return this.wrapper.getAccessWrapper(type).noAccess();
	}

	public String getNoRoleMessage() {
		return this.getNoMessage(WrapperType.ROLE);
	}

	public String getNoUserMessage() {
		return this.getNoMessage(WrapperType.USER);
	}

	public String getNoGuildMessage() {
		return this.getNoMessage(WrapperType.GUILD);
	}

	public String getNoChannelMessage() {
		return this.getNoMessage(WrapperType.CHANNEL);
	}

	private <A extends Annotation> String getNoMessage(WrapperType<A> type) {
		return this.wrapper.getAccessWrapper(type).noAccessMessage();
	}
}
