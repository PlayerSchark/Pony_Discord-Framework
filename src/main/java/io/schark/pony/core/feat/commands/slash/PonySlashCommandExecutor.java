package io.schark.pony.core.feat.commands.slash;

import io.schark.pony.core.feat.commands.executor.PonyCommandExecutor;

/**
 * @author Player_Schark
 */
public abstract class PonySlashCommandExecutor extends PonyCommandExecutor {

	public PonySlashCommandExecutor(String rawLabel) {
		super(rawLabel);
	}
}
