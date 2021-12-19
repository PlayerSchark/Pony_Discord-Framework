package io.schark.pony.core.feat.commands.executor;

import io.schark.pony.core.feat.commands.command.PonyCommandBase;
import io.schark.pony.core.feat.commands.in.PonyArg;
import net.dv8tion.jda.api.events.Event;

/**
 * @author Player_Schark
 */
public interface PonyCommandExecutable {

	String getRawLabel();
	String ponyExecute(PonyCommandBase<? extends Event, ? extends PonyArg<?>> command);
	String getDescription();
}
