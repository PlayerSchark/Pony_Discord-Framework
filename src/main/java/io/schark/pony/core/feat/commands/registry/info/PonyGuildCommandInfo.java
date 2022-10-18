package io.schark.pony.core.feat.commands.registry.info;

import io.schark.pony.core.feat.commands.executor.input.PonyGuildCommandExecutable;
import io.schark.pony.core.feat.commands.registry.wrapper.PonyAnnotationWrapper;
import lombok.Getter;

/**
 * @author Player_Schark
 */
@Getter
public class PonyGuildCommandInfo extends PonyDiscordCommandInfo<PonyGuildCommandExecutable> {

	public PonyGuildCommandInfo(PonyGuildCommandExecutable executable, PonyAnnotationWrapper wrapper, boolean botUsable,
                              boolean caseSensitive, boolean blacklisted, boolean sendThinking, boolean ephemeral) {
		super(executable, wrapper, botUsable, caseSensitive, blacklisted, sendThinking, ephemeral);
	}
}
