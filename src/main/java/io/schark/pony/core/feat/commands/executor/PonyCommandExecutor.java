package io.schark.pony.core.feat.commands.executor;

import io.schark.pony.core.feat.commands.command.PonyCommandBase;
import io.schark.pony.core.feat.commands.in.PonyArg;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.Event;

/**
 * @author Player_Schark
 */
@RequiredArgsConstructor
@Getter
public abstract class PonyCommandExecutor implements PonyCommandExecutable {

	private final String rawLabel;
	private final String description;

	@Override public abstract String ponyExecute(PonyCommandBase<? extends Event, ? extends PonyArg<?>> command);
}
