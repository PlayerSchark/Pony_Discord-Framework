package io.schark.pony.core.feat.commands.command;

import io.schark.pony.core.feat.commands.annotation.impl.PonyRunnable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

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
	@Nullable private final PonyRunnable noRole;
	@Nullable private final PonyRunnable noUser;
	@Nullable private final PonyRunnable noGuild;
	@Nullable private final PonyRunnable noChannel;
	@Nullable private final String noRoleMessage;
	@Nullable private final String noUserMessage;
	@Nullable private final String noGuildMessage;
	@Nullable private final String noChannelMessage;
}
