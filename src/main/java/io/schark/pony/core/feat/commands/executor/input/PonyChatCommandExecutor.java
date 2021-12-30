package io.schark.pony.core.feat.commands.executor.input;

import io.schark.pony.core.feat.commands.command.PonyChatCommand;
import io.schark.pony.core.feat.commands.command.PonyCommand;
import io.schark.pony.core.feat.commands.command.PonyGuildCommand;
import io.schark.pony.core.feat.commands.executor.PonyCommandExecutor;
import io.schark.pony.core.feat.commands.in.PonyArg;
import lombok.Getter;
import net.dv8tion.jda.api.events.Event;

/**
 * @author Player_Schark
 */
@Getter
public abstract class PonyChatCommandExecutor extends PonyCommandExecutor implements PonyChatCommandExecutable {

	private final boolean guildCommand;

	public PonyChatCommandExecutor(String label, String description) {
		this(label, description, false);
	}

	public PonyChatCommandExecutor(String label, String description, boolean publicCommand) {
		super(label, description);
		this.guildCommand = publicCommand;
	}

	@Override public String ponyExecute(PonyCommand<? extends Event, ? extends PonyArg<?>> command) {
		return switch (command) {
			case PonyGuildCommand cmd -> this.executeCommand(cmd);
			case PonyChatCommand cmd -> this.executeCommand(cmd);
			default -> throw new IllegalStateException("Unexpected value: " + command);
		};
	}

	private String executeCommand(PonyGuildCommand cmd) {
		if (this instanceof PonyGuildCommandExecutable executable) {
			return executable.executeCommand(cmd);
		}
		else {
			throw new IllegalStateException("PonyChatCommandExecutor must implement PonyGuildCommandExecutable");
		}
	}

	@Override public abstract String executeCommand(PonyChatCommand command);
}
