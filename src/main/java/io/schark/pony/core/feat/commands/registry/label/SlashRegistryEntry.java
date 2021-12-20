package io.schark.pony.core.feat.commands.registry.label;

import io.schark.pony.core.feat.commands.command.info.PonySlashCommandInfo;
import io.schark.pony.core.feat.commands.executor.PonySlashCommandExecutable;

/**
 * @author Player_Schark
 */
public class SlashRegistryEntry extends DiscordRegistryEntry<PonySlashCommandExecutable, PonySlashCommandInfo> {

	public SlashRegistryEntry(String rawLabel, PonySlashCommandInfo commandInfo) {
		super(rawLabel, commandInfo);
	}
}
