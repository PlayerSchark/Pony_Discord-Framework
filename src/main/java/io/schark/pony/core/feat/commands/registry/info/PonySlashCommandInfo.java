package io.schark.pony.core.feat.commands.registry.info;

import io.schark.pony.core.feat.commands.executor.input.PonySlashCommandExecutable;
import io.schark.pony.core.feat.commands.registry.wrapper.PonyAnnotationWrapper;

/**
 * @author Player_Schark
 */
public class PonySlashCommandInfo extends PonyDiscordCommandInfo<PonySlashCommandExecutable> {

	public PonySlashCommandInfo(PonySlashCommandExecutable executable, PonyAnnotationWrapper wrapper, boolean botUsable,
                              boolean caseSensitive, boolean blacklisted, boolean sendThinking, boolean ephemeral) {
		super(executable, wrapper, botUsable, caseSensitive, blacklisted, sendThinking, ephemeral);
	}
}
