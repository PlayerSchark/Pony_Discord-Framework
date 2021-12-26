package io.schark.pony.core.feat.commands.executor.action;

/**
 * @author Player_Schark
 */
public abstract class PonyUserCommandExecutor extends PonyActionCommandExecutor implements PonyUserCommandExecutable {

	public PonyUserCommandExecutor(String rawLabel, String description) {
		super(rawLabel, description);
	}
}
