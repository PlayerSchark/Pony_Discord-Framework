package io.schark.pony.core.feat.commands.registry.label;

import io.schark.pony.core.feat.commands.command.info.PonyDiscordCommandInfo;
import io.schark.pony.core.feat.commands.executor.PonyDiscordCommandExecutable;

/**
 * @author Player_Schark
 */
public abstract class DiscordRegistryEntry<E extends PonyDiscordCommandExecutable, I extends PonyDiscordCommandInfo<E>> extends RegistryEntry<E, I> {

	public DiscordRegistryEntry(String rawLabel, I commandInfo) {
		super(rawLabel, commandInfo);
	}

	@Override
	public I getCommandInfo() {
		return super.getCommandInfo();
	}
}
