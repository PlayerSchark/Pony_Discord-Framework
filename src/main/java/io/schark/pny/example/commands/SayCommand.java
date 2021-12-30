package io.schark.pny.example.commands;

import com.google.common.base.Joiner;
import io.schark.pony.core.feat.commands.command.PonyChatCommand;
import io.schark.pony.core.feat.commands.executor.input.PonyChatCommandExecutor;

import java.util.List;

/**
 * @author Player_Schark
 */
public class SayCommand extends PonyChatCommandExecutor {

	public SayCommand() {
		super("say", "wiederholt alles was du schreibst :)", true);
	}

	@Override public String executeCommand(PonyChatCommand command) {
		List<String> rawArguments = command.getRawArguments();
		return Joiner.on(" ").join(rawArguments);
	}
}
