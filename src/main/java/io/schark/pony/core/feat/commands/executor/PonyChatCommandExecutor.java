package io.schark.pony.core.feat.commands.executor;

import io.schark.pony.core.feat.commands.comp.PonyCommandComponents;
import io.schark.pony.core.feat.commands.comp.PonyComponentType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Player_Schark
 */
@Getter
public abstract class PonyChatCommandExecutor extends PonyCommandExecutor {

	private final ArrayList<PonyCommandComponents> components = new ArrayList<>();
	private final boolean guildCommand;

	public PonyChatCommandExecutor(String label) {
		this(label, true);
	}

	public PonyChatCommandExecutor(String label, boolean publicCommand) {
		super(label);
		this.guildCommand = publicCommand;
	}

	public PonyChatCommandExecutor(String label, PonyCommandComponents... args) {
		super(label);
		this.guildCommand = true;
		this.components.addAll(Arrays.asList(args));
	}

	public void addArgument(PonyCommandComponents arg) {
		this.components.add(arg);
	}

	public PonyCommandComponents getComponents() {
		return new PonyCommandComponents("say", "hello there", PonyComponentType.COMMAND);
	}
}
