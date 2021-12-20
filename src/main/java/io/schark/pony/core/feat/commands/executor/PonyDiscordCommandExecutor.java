package io.schark.pony.core.feat.commands.executor;

/**
 * @author Player_Schark
 */
public abstract class PonyDiscordCommandExecutor extends PonyCommandExecutor implements PonyDiscordCommandExecutable {

	public PonyDiscordCommandExecutor(String rawLabel, String description) {
		super(rawLabel, description);
	}
}
