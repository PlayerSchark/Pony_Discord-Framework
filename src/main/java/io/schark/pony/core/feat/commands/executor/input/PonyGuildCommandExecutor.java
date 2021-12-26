package io.schark.pony.core.feat.commands.executor.input;

import io.schark.pony.core.feat.commands.command.PonyCommand;
import io.schark.pony.core.feat.commands.command.PonyDiscordCommand;
import io.schark.pony.core.feat.commands.in.PonyArg;
import lombok.Getter;
import net.dv8tion.jda.api.events.Event;

/**
 * @author Player_Schark
 */
@Getter
public abstract class PonyGuildCommandExecutor extends PonyDiscordCommandExecutor implements PonyGuildCommandExecutable {

	private long guildId;

	public PonyGuildCommandExecutor(String rawLabel, String description, long guildId) {
		super(rawLabel, description);
		this.guildId = guildId;
	}

	@Override public String ponyExecute(PonyCommand<? extends Event, ? extends PonyArg<?>> command) {
		return this.executeCommand((PonyDiscordCommand) command);
	}

	@Override
	public long[] guildIds() {
		return new long[]{this.guildId};
	}
}
