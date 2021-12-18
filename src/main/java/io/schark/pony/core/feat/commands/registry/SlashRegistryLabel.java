package io.schark.pony.core.feat.commands.registry;

import io.schark.pony.core.feat.commands.command.CommandInfo;

import java.util.Set;

/**
 * @author Player_Schark
 */
public class SlashRegistryLabel extends RegistryLabel {

	public SlashRegistryLabel(String rawLabel, Set<String> aliases, CommandInfo commandInfo) {
		super(rawLabel, aliases, commandInfo);
	}
}
