package io.schark.pony.core.feat.commands.registry.label;

import io.schark.pony.core.feat.commands.executor.input.PonyGuildCommandExecutable;
import io.schark.pony.core.feat.commands.registry.info.PonyGuildCommandInfo;
import lombok.Getter;

import java.util.Set;

/**
 * @author Player_Schark
 */
@Getter
public class PonyGuildRegistryEntry extends PonyDiscordRegistryEntry<PonyGuildCommandExecutable, PonyGuildCommandInfo> {

	private final Set<Long> guildIds;

	public PonyGuildRegistryEntry(String rawLabel, PonyGuildCommandInfo commandInfo, Set<Long> guildIds) {
		super(rawLabel, commandInfo);
		this.guildIds = guildIds;
	}
}
