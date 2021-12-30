package io.schark.pony.core.feat.commands.registry;

import io.schark.pony.core.feat.commands.registry.label.*;

/**
 * @author Player_Schark
 */
public record PonyRegistryType<T extends PonyRegistryEntry>(Class<T> typeClass) {

	public static final PonyRegistryType<PonyChatRegistryEntry> CHAT = new PonyRegistryType<>(PonyChatRegistryEntry.class);
	public static final PonyRegistryType<PonyDiscordRegistryEntry> DISCORD = new PonyRegistryType<>(PonyDiscordRegistryEntry.class);
	public static final PonyRegistryType<PonyGuildRegistryEntry> GUILD = new PonyRegistryType<>(PonyGuildRegistryEntry.class);
	public static final PonyRegistryType<PonySlashRegistryEntry> SLASH = new PonyRegistryType<>(PonySlashRegistryEntry.class);

	public boolean isInstance(PonyRegistryEntry entry) {
		return this.typeClass().isInstance(entry);
	}

	public T cast(PonyRegistryEntry entry) {
		return this.typeClass.cast(entry);
	}
}
