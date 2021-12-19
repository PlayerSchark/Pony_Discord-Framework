package io.schark.pony.core.feat.commands.executor;

import io.schark.pony.core.feat.commands.command.PonyCommandBase;
import io.schark.pony.core.feat.commands.command.PonyGuildCommand;
import io.schark.pony.core.feat.commands.in.PonyArg;
import lombok.Getter;
import net.dv8tion.jda.api.events.Event;

/**
 * @author Player_Schark
 */
@Getter
public abstract class PonyGuildCommandExecutor extends PonyCommandExecutor implements PonyGuildCommandExecutable {

	private long guildId;

	public PonyGuildCommandExecutor(String rawLabel, String description, long guildId) {
		super(rawLabel, description);
		this.guildId = guildId;
	}

	@Override public String ponyExecute(PonyCommandBase<? extends Event, ? extends PonyArg<?>> command) {
		return this.executeCommand((PonyGuildCommand) command);
	}

	@Override
	public long[] guildIds() {
		return new long[]{this.guildId};
	}
}
