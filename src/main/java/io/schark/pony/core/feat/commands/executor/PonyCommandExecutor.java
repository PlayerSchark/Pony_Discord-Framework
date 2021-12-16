package io.schark.pony.core.feat.commands.executor;

import io.schark.pony.core.feat.commands.command.PonyCommand;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Player_Schark
 */
@RequiredArgsConstructor
@Getter
public abstract class PonyCommandExecutor {

	private final String rawLabel;

	public abstract String execute(PonyCommand command);

}
