package io.schark.pony.core.feat.commands.command.info;

import io.schark.pony.core.feat.commands.annotation.impl.PonyRunnable;
import io.schark.pony.core.feat.commands.executor.PonyChatCommandExecutor;
import io.schark.pony.core.feat.commands.executor.PonyCommandExecutable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.BiFunction;

/**
 * @author Player_Schark
 */
@Getter
@RequiredArgsConstructor
public abstract class PonyCommandInfo<E extends PonyCommandExecutable> {

	private final E executable;
	private final Set<Long> allowedRoles;
	private final Set<Long> allowedUsers;
	private final Set<Long> allowedGuilds;
	private final Set<Long> allowedChannels;
	private final boolean botUsable;
	private final boolean sendTyping;
	private final boolean caseSensitive;
	private final boolean blacklisted;
	@Nullable private final PonyRunnable noRole;
	@Nullable private final PonyRunnable noUser;
	@Nullable private final PonyRunnable noGuild;
	@Nullable private final PonyRunnable noChannel;
	@Nullable private final String noRoleMessage;
	@Nullable private final String noUserMessage;
	@Nullable private final String noGuildMessage;
	@Nullable private final String noChannelMessage;

	protected <Ev extends Event> void runNoAccess(Ev event, String noAccessMessage, BiFunction<Ev, String, RestAction> action, PonyRunnable runnable) {
		if (runnable != null) {
			runnable.run();
		}
		if (noAccessMessage != null && !noAccessMessage.isEmpty()) {
			action.apply(event, noAccessMessage).queue();
		}
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted") <Ev extends Event> boolean hasAccess(Ev e, BiFunction<Ev, String, RestAction> noAccessFunction, User author, Member member,
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
					this.runNoAccess(e, this.getNoGuildMessage(), noAccessFunction, this.getNoGuild());
					return false;
				}

				//role check
				if (blacklisted != this.hasAccess(this.getAllowedRoles(), member)) {
					asMember = true;
				}
				else {
					this.runNoAccess(e, this.getNoRoleMessage(), noAccessFunction, this.getNoRole());
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
		boolean asUser = this.hasUserAccess(e, noAccessFunction, this, author, asMember);

		//channels
		boolean asChannel = this.hasChannelAccess(e, noAccessFunction, this, channel, asUser);
		return asMember && asChannel && asUser;
	}

	private <Ev extends Event> boolean hasChannelAccess(Ev event, BiFunction<Ev, String, RestAction> noAccessFunction, PonyCommandInfo<E> commandInfo, MessageChannel channel, boolean runNoAccess) {
		return this.hasIdAccess(event, noAccessFunction, channel.getIdLong(), commandInfo.getAllowedChannels(), commandInfo.getNoChannel(), commandInfo.getNoChannelMessage(), runNoAccess);
	}

	private <Ev extends Event> boolean hasUserAccess(Ev event, BiFunction<Ev, String, RestAction> noAccessFunction, PonyCommandInfo<E> commandInfo, User author, boolean runNoAccess) {
		return this.hasIdAccess(event, noAccessFunction, author.getIdLong(), commandInfo.getAllowedUsers(), commandInfo.getNoUser(), commandInfo.getNoUserMessage(), runNoAccess);
	}

	private <Ev extends Event> boolean hasIdAccess(Ev event, BiFunction<Ev, String, RestAction> noAccessFunction, Long id, Set<Long> allowedUsers,
					@Nullable PonyRunnable ponyRunnable, @Nullable String message, boolean runNoAccess) {
		boolean as = allowedUsers.isEmpty() || allowedUsers.contains(id);
		as = this.blacklisted != as;

		if (!as && runNoAccess) {
			this.runNoAccess(event, message, noAccessFunction, ponyRunnable);
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
}
