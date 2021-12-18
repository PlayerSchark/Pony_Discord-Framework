package io.schark.pony.core.feat.commands.executor;

/**
 * @author Player_Schark
 */
public abstract class PonyGuildCommandExecutor extends PonyCommandExecutor {

	private long guildId;
	public PonyGuildCommandExecutor(String rawLabel, long guildId) {
		super(rawLabel);
		this.guildId = guildId;
	}
}
