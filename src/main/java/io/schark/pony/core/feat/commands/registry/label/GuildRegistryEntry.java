package io.schark.pony.core.feat.commands.registry.label;

import io.schark.pony.core.feat.commands.command.info.PonyGuildCommandInfo;
import io.schark.pony.core.feat.commands.executor.PonyGuildCommandExecutable;
import lombok.Getter;

import java.util.Set;

/**
 * @author Player_Schark
 */
@Getter
public class GuildRegistryEntry extends DiscordRegistryEntry<PonyGuildCommandExecutable, PonyGuildCommandInfo> {

	private final Set<Long> guildIds;

	public GuildRegistryEntry(String rawLabel, PonyGuildCommandInfo commandInfo, Set<Long> guildIds) {
		super(rawLabel, commandInfo);
		this.guildIds = guildIds;
	}

}
