package io.schark.pony.core.feat.commands.registry.label;

import io.schark.pony.core.feat.commands.executor.input.PonyDiscordCommandExecutable;
import io.schark.pony.core.feat.commands.registry.info.PonyDiscordCommandInfo;

/**
 * @author Player_Schark
 */
public non-sealed abstract class PonyDiscordRegistryEntry<E extends PonyDiscordCommandExecutable, I extends PonyDiscordCommandInfo<E>> extends PonyRegistryEntry<E, I> {

	public PonyDiscordRegistryEntry(String rawLabel, I commandInfo) {
		super(rawLabel, commandInfo);
	}

	@SuppressWarnings("EmptyMethod") @Override
	public I getCommandInfo() {
		//nothing
		return super.getCommandInfo();
	}
}
