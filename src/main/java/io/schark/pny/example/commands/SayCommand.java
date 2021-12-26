package io.schark.pny.example.commands;

import com.google.common.base.Joiner;
import io.schark.pony.core.feat.commands.annotation.Alias;
import io.schark.pony.core.feat.commands.annotation.access.AllowedChannels;
import io.schark.pony.core.feat.commands.annotation.access.AllowedUsers;
import io.schark.pony.core.feat.commands.command.PonyChatCommand;
import io.schark.pony.core.feat.commands.executor.input.PonyChatCommandExecutor;

import java.util.List;

/**
 * @author Player_Schark
 */
public class SayCommand extends PonyChatCommandExecutor {

	@Alias(aliases = { "tell", "announce", "sai" })
	@AllowedUsers(ids = 276812593463820289L, noAccessMessage = "sorry you are blocked")
	@AllowedChannels(ids = 847434700708642826L, noAccessMessage = "you are in channel")
	//@AllowedRoles(ids = 15L)
	public SayCommand() {
		super("say", "wiederholt alles was du schreibst :)", true);
	}

	@Override public String executeCommand(PonyChatCommand command) {
		//	return this.execute(command);
		//}
		/*
	@Override public String executeCommand(PonySlashCommand command) {
		return this.execute(command);
	}
	*/

		//private String execute(IPonyCommand command) {

		List<String> rawArguments = command.getRawArguments();
		return Joiner.on(" ").join(rawArguments);
	}
	/*

	public long[] guildIds() {
		return new long[]{839579270933774336L};
	}


	@Override
	public OptionData withSingleOptionData() {
		return new OptionData(OptionType.STRING, "message", "Deine Nachricht", true);
	}

	@Override public void reject(PonyGuildCommand command) {

	}
	*/
}
