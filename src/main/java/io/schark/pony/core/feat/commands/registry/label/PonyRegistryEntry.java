package io.schark.pony.core.feat.commands.registry.label;

import io.schark.pony.core.feat.commands.executor.PonyCommandExecutable;
import io.schark.pony.core.feat.commands.registry.info.PonyCommandInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Player_Schark
 */
@Getter
@RequiredArgsConstructor
public sealed abstract class PonyRegistryEntry<E extends PonyCommandExecutable, I extends PonyCommandInfo<E>> permits PonyChatRegistryEntry, PonyDiscordRegistryEntry {

	private final String rawLabel;
	private final I commandInfo;

	public boolean matches(String label) {
		return this.matches(this.rawLabel, label);
	}

	public boolean matches(@NotNull String check, @Nullable String label) {
		return this.commandInfo.isCaseSensitive() ? check.equals(label) : check.equalsIgnoreCase(label);
	}

}
