package io.schark.pony.feat.commands;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Player_Schark
 */
@Getter
public abstract class PonyChatCommandExecutor extends PonyCommandExecutor {

	private final String rawLabel;
	private final ArrayList<PonyCommandComponent> components = new ArrayList<>();
	private final boolean guildCommand;

	public PonyChatCommandExecutor(String label) {
		this(label, true);
	}

	public PonyChatCommandExecutor(String label, boolean publicCommand) {
		this.rawLabel = label;
		this.guildCommand = publicCommand;
	}

	public PonyChatCommandExecutor(String label, PonyCommandComponent... args) {
		this.rawLabel = label;
		this.guildCommand = true;
		this.components.addAll(Arrays.asList(args));
	}

	public void addArgument(PonyCommandComponent arg) {
		this.components.add(arg);
	}
}
