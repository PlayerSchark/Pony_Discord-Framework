package io.schark.pony.core.feat.commands.command.info;

import io.schark.pony.core.feat.commands.annotation.impl.PonyRunnable;
import io.schark.pony.core.feat.commands.executor.PonyChatCommandExecutable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.BiFunction;

/**
 * @author Player_Schark
 */
public class PonyChatCommandInfo extends PonyCommandInfo<PonyChatCommandExecutable> {

	public PonyChatCommandInfo(PonyChatCommandExecutable executable, Set<Long> allowedRoles, Set<Long> allowedUsers, Set<Long> allowedGuilds,
					Set<Long> allowedChannels, boolean botUsable, boolean sendTyping, boolean caseSensitive, boolean blacklisted,
					@Nullable PonyRunnable noRole,
					@Nullable PonyRunnable noUser,
					@Nullable PonyRunnable noGuild,
					@Nullable PonyRunnable noChannel, @Nullable String noRoleMessage,
					@Nullable String noUserMessage, @Nullable String noGuildMessage,
					@Nullable String noChannelMessage) {
		super(executable, allowedRoles, allowedUsers, allowedGuilds, allowedChannels, botUsable, sendTyping, caseSensitive, blacklisted, noRole, noUser, noGuild, noChannel, noRoleMessage,
						noUserMessage,
						noGuildMessage, noChannelMessage);
	}

	public boolean hasAccess(MessageReceivedEvent e) {
		Message message = e.getMessage();
		BiFunction<Event, String, RestAction> noAccessFunction = (ev, msg) -> message.getChannel().sendMessage(msg);
		return super.hasAccess(e, noAccessFunction, message.getAuthor(), e.getMember(), message.getChannel());
	}
}
