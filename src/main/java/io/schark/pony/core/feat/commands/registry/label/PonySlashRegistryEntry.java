package io.schark.pony.core.feat.commands.registry.label;

import io.schark.pony.core.feat.commands.executor.input.PonySlashCommandExecutable;
import io.schark.pony.core.feat.commands.registry.info.PonySlashCommandInfo;

/**
 * @author Player_Schark
 */
public class PonySlashRegistryEntry extends PonyDiscordRegistryEntry<PonySlashCommandExecutable, PonySlashCommandInfo> {

	public PonySlashRegistryEntry(String rawLabel, PonySlashCommandInfo commandInfo) {
		super(rawLabel, commandInfo);
	}
}
