package io.schark.pony.core.feat.commands.executor.input;

import io.schark.pony.core.feat.commands.command.PonyChatCommand;
import io.schark.pony.core.feat.commands.executor.PonyCommandExecutable;

/**
 * @author Player_Schark
 */
public interface PonyChatCommandExecutable extends PonyCommandExecutable {

	String executeCommand(PonyChatCommand command);
}
