package io.schark.pny.example.commands;

import io.schark.pony.core.feat.commands.command.PonyGuildCommand;
import io.schark.pony.core.feat.commands.executor.PonyGuildCommandExecutor;

/**
 * @author Player_Schark
 */
public class AnotherSayCommand extends PonyGuildCommandExecutor {

	public AnotherSayCommand() {
		super("another", "Does the same as say", 839579270933774336L);
	}

	@Override public String executeCommand(PonyGuildCommand command) {
		return "Hello there :D";
	}
}
