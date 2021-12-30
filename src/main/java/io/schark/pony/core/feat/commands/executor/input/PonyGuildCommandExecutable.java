package io.schark.pony.core.feat.commands.executor.input;

import io.schark.pony.core.feat.commands.annotation.impl.PonyAccessor;
import io.schark.pony.core.feat.commands.command.PonyDiscordCommand;
import io.schark.pony.core.feat.commands.command.PonyGuildCommand;

/**
 * @author Player_Schark
 */
public interface PonyGuildCommandExecutable extends PonyDiscordCommandExecutable {

	String executeCommand(PonyGuildCommand command);

	@Override default String executeCommand(PonyDiscordCommand command) {
		return this.executeCommand((PonyGuildCommand) command);
	}

	long[] guildIds();
	default Class<? extends PonyAccessor> accessor() {
		return PonyAccessor.class;
	}
}
