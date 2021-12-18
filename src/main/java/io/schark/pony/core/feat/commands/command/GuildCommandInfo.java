package io.schark.pony.core.feat.commands.command;

import io.schark.pony.core.feat.commands.annotation.impl.PonyRunnable;
import lombok.Getter;

import java.util.Set;

/**
 * @author Player_Schark
 */
@Getter
public class GuildCommandInfo extends CommandInfo {

	private boolean sendThinking;
	private boolean ephemeral;

	public GuildCommandInfo(Set<Long> allowedRoles, Set<Long> allowedUsers, Set<Long> allowedGuilds, Set<Long> allowedChannels, boolean botUsable,
					boolean caseSensitive,
					boolean sendThinking,
					boolean ephemeral,
					PonyRunnable noRole,
					PonyRunnable noUser,
					PonyRunnable noGuild,
					PonyRunnable noChannel,
					String noRoleMessage,
					String noUserMessage,
					String noGuildMessage,
					String noChannelMessage
					) {
		super(allowedRoles, allowedUsers, allowedGuilds, allowedChannels, botUsable, false, caseSensitive, noRole, noUser, noGuild, noChannel, noRoleMessage, noUserMessage, noGuildMessage, noChannelMessage);
		this.sendThinking = sendThinking;
		this.ephemeral = ephemeral;
	}
}
