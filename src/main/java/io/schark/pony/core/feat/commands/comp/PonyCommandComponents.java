package io.schark.pony.core.feat.commands.comp;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.ArrayList;

/**
 * @author Player_Schark
 */

public class PonyCommandComponents extends CommandData {
	@Getter private final String name;
	@Getter private final PonyComponentType type;
	private final ArrayList<PonyCommandComponents> nextArgs = new ArrayList<>();

	public PonyCommandComponents(String name, String description, PonyComponentType type) {
		super(name, description);
		this.name = name;
		this.type = type;
	}

	public PonyCommandComponents addNextArgument(PonyCommandComponents next) {
		this.nextArgs.add(next);
		return this;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<PonyCommandComponents> getNextArguments() {
		return (ArrayList<PonyCommandComponents>) this.nextArgs.clone();
	}
}
