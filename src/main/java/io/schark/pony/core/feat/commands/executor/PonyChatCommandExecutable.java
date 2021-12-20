package io.schark.pony.core.feat.commands.executor;

import io.schark.pony.core.feat.commands.command.PonyChatCommand;

/**
 * @author Player_Schark
 */
public interface PonyChatCommandExecutable extends PonyCommandExecutable {

	String executeCommand(PonyChatCommand command);
}
