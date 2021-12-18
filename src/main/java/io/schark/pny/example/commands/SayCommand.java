package io.schark.pny.example.commands;

import io.schark.pony.core.feat.commands.annotation.Alias;
import io.schark.pony.core.feat.commands.annotation.GuildCommand;
import io.schark.pony.core.feat.commands.command.PonyCommand;
import io.schark.pony.core.feat.commands.executor.PonyChatCommandExecutor;

/**
 * @author Player_Schark
 */
public class SayCommand extends PonyChatCommandExecutor {

	@Alias(aliases = {"tell", "announce", "sai"})
	//@AllowedRoles(ids = 15L)
	@GuildCommand(guildIds = {839579270933774336L})
	public SayCommand() {
		super("say");
	}

	@Override public String execute(PonyCommand command) {
		return null;
	}
}
