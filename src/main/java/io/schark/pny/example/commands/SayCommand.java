package io.schark.pny.example.commands;

import com.google.common.base.Joiner;
import io.schark.pony.core.feat.commands.annotation.Alias;
import io.schark.pony.core.feat.commands.command.IPonyCommand;
import io.schark.pony.core.feat.commands.command.PonyChatCommand;
import io.schark.pony.core.feat.commands.command.PonyGuildCommand;
import io.schark.pony.core.feat.commands.executor.PonyChatCommandExecutor;
import io.schark.pony.core.feat.commands.executor.PonyGuildCommandExecutable;

import java.util.List;

/**
 * @author Player_Schark
 */
public class SayCommand extends PonyChatCommandExecutor implements PonyGuildCommandExecutable {

	@Alias(aliases = {"tell", "announce", "sai"})
	//@AllowedRoles(ids = 15L)
	public SayCommand() {
		super("say", "wiederholt alles was du schreibst :)");
	}

	@Override public String executeCommand(PonyChatCommand command) {
		return this.execute(command);
	}

	@Override public String executeCommand(PonyGuildCommand command) {
		return this.execute(command);
	}

	private String execute(IPonyCommand command) {

		List<String> rawArguments = command.getRawArguments();
		return Joiner.on(" ").join(rawArguments);
	}

	@Override public long[] guildIds() {
		return new long[]{839579270933774336L};
	}

	/*
	@Override
	public OptionData withSingleOptionData() {
		return new OptionData(OptionType.STRING, "message", "Deine Nachricht", true);
	}*/
}
