package io.schark.pony.core.feat.commands.executor.action;

import io.schark.pony.core.feat.commands.executor.PonyCommandExecutor;

/**
 * @author Player_Schark
 */
public abstract class PonyActionCommandExecutor extends PonyCommandExecutor {

	public PonyActionCommandExecutor(String rawLabel, String description) {
		super(rawLabel, description);
	}
}
