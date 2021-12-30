package io.schark.pony.core.feat.commands.executor.input;

import io.schark.pony.core.feat.commands.command.PonyDiscordCommand;
import io.schark.pony.core.feat.commands.command.PonySlashCommand;

/**
 * @author Player_Schark
 */
public interface PonySlashCommandExecutable extends PonyDiscordCommandExecutable {
	String executeCommand(PonySlashCommand command);

	@Override default String executeCommand(PonyDiscordCommand command) {
		return this.executeCommand((PonySlashCommand) command);
	}
}
