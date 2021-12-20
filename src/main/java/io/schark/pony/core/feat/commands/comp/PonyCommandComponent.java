package io.schark.pony.core.feat.commands.comp;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.ArrayList;

/**
 * @author Player_Schark
 */

public abstract class PonyCommandComponent extends CommandData {
	private final ArrayList<PonyCommandComponent> nextArgs = new ArrayList<>();

	public PonyCommandComponent(String name, String description) {
		super(name, description);
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
