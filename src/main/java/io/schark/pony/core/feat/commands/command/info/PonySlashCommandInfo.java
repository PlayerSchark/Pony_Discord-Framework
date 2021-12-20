package io.schark.pony.core.feat.commands.command.info;

import io.schark.pony.core.feat.commands.annotation.impl.PonyRunnable;
import io.schark.pony.core.feat.commands.executor.PonySlashCommandExecutable;

import java.util.Set;

/**
 * @author Player_Schark
 */
public class PonySlashCommandInfo extends PonyDiscordCommandInfo<PonySlashCommandExecutable> {

	public PonySlashCommandInfo(PonySlashCommandExecutable executable, Set<Long> allowedRoles, Set<Long> allowedUsers, Set<Long> allowedGuilds, Set<Long> allowedChannels, boolean botUsable,
					boolean caseSensitive, boolean blacklisted, boolean sendThinking, boolean ephemeral, PonyRunnable noRole, PonyRunnable noUser, PonyRunnable noGuild,
					PonyRunnable noChannel, String noRoleMessage, String noUserMessage, String noGuildMessage, String noChannelMessage) {
		super(executable, allowedRoles, allowedUsers, allowedGuilds, allowedChannels, botUsable, caseSensitive, blacklisted, sendThinking, ephemeral, noRole, noUser, noGuild, noChannel, noRoleMessage,
						noUserMessage,
						noGuildMessage, noChannelMessage);
	}
}
