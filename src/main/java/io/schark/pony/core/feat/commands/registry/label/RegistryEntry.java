package io.schark.pony.core.feat.commands.registry.label;

import io.schark.pony.core.feat.commands.command.info.PonyCommandInfo;
import io.schark.pony.core.feat.commands.executor.PonyCommandExecutable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Player_Schark
 */
@Getter
@RequiredArgsConstructor
public abstract class RegistryEntry<E extends PonyCommandExecutable, I extends PonyCommandInfo<E>> {

	private final String rawLabel;
	private final I commandInfo;

	public boolean matches(String label) {
		return this.matches(this.rawLabel, label);
	}

	public boolean matches(@NotNull String check, @Nullable String label) {
		return this.commandInfo.isCaseSensitive() ? check.equals(label) : check.equalsIgnoreCase(label);
	}

}
