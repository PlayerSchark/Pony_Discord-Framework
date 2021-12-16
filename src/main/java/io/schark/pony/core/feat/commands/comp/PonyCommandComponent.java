package io.schark.pony.core.feat.commands.comp;
import lombok.Getter;

import java.util.ArrayList;

/**
 * @author Player_Schark
 */

public abstract class PonyCommandComponent {
	@Getter private final String content;
	@Getter private final PonyComponentType type;
	private final ArrayList<PonyCommandComponent> nextArgs = new ArrayList<>();

	public PonyCommandComponent(String content, PonyComponentType type) {
		this.content = content;
		this.type = type;
	}

	public PonyCommandComponent addNextArgument(PonyCommandComponent next) {
		this.nextArgs.add(next);
		return this;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<PonyCommandComponent> getNextArguments() {
		return (ArrayList<PonyCommandComponent>) this.nextArgs.clone();
	}
}
