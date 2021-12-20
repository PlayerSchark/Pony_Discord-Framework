package io.schark.pony.core.feat.commands.executor;

import io.schark.pony.core.feat.commands.command.PonyCommand;
import io.schark.pony.core.feat.commands.command.PonySlashCommand;
import io.schark.pony.core.feat.commands.in.PonyArg;
import net.dv8tion.jda.api.events.Event;

/**
 * @author Player_Schark
 */
public abstract class PonySlashCommandExecutor extends PonyDiscordCommandExecutor implements PonySlashCommandExecutable {

	public PonySlashCommandExecutor(String rawLabel, String description) {
		super(rawLabel, description);
	}

	@Override public String ponyExecute(PonyCommand<? extends Event, ? extends PonyArg<?>> command) {
		return this.executeCommand((PonySlashCommand) command);
	}
}
