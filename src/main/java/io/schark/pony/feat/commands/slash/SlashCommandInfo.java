package io.schark.pony.feat.commands.slash;

import io.schark.pony.feat.commands.command.CommandInfo;
import lombok.Getter;

import java.util.Set;

/**
 * @author Player_Schark
 */
@Getter
public class SlashCommandInfo extends CommandInfo {

	private boolean sendThinking;
	private boolean ephemeral;

	public SlashCommandInfo(Set<Long> allowedRoles, Set<Long> allowedUsers, Set<Long> allowedGuilds, Set<Long> allowedChannels, boolean botUsable,
					boolean sendTyping,
					boolean caseSensitive,
					boolean sendThinking,
					boolean ephemeral) {
		super(allowedRoles, allowedUsers, allowedGuilds, allowedChannels, botUsable, sendTyping, caseSensitive);
		this.sendThinking = sendThinking;
		this.ephemeral = ephemeral;
	}
}
