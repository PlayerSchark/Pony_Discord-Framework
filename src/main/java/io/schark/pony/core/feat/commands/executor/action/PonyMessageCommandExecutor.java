package io.schark.pony.core.feat.commands.executor.action;

/**
 * @author Player_Schark
 */
public abstract class PonyMessageCommandExecutor extends PonyActionCommandExecutor implements PonyMessageCommandExecutable {

	public PonyMessageCommandExecutor(String rawLabel, String description) {
		super(rawLabel, description);
	}
}
