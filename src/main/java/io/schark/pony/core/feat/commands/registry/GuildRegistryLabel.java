package io.schark.pony.core.feat.commands.registry;

import io.schark.pony.core.feat.commands.command.CommandInfo;
import lombok.Getter;

import java.util.Set;

/**
 * @author Player_Schark
 */
@Getter
public class GuildRegistryLabel extends RegistryLabel {

	private final Set<Long>  guildIds;

	public GuildRegistryLabel(String rawLabel, Set<String> aliases, CommandInfo commandInfo, Set<Long> guildIds) {
		super(rawLabel, aliases, commandInfo);
		this.guildIds = guildIds;
	}

}
