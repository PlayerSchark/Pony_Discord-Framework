package io.schark.pony.core.feat.commands.command;

import io.schark.pony.core.feat.commands.annotation.impl.PonyFunction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * @author Player_Schark
 */
@Getter
@RequiredArgsConstructor
public class CommandInfo {

	private final Set<Long> allowedRoles;
	private final Set<Long> allowedUsers;
	private final Set<Long> allowedGuilds;
	private final Set<Long> allowedChannels;
	private final boolean botUsable;
	private final boolean sendTyping;
	private final boolean caseSensitive;
	private final PonyFunction noRole;
	private final PonyFunction noUser;
	private final PonyFunction noGuild;
	private final PonyFunction noChannel;
}
