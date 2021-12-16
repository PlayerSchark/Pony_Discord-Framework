package io.schark.pony.core.feat.commands.registry;

import io.schark.pony.core.feat.commands.command.CommandInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * @author Player_Schark
 */
@Getter
@RequiredArgsConstructor
public class RegistryLabel {

	private final String rawLabel;
	private final Set<String> aliases;
	private final CommandInfo commandInfo;

	public boolean matches(String label) {
		boolean matchesRaw = this.matches(this.rawLabel, label);
		if (matchesRaw) {
			return true;
		}
		for (String alias : this.aliases) {
			if (this.matches(alias, label)) {
				return true;
			}
		}
		return false;
	}

	private boolean matches(String check, String label) {
		return this.commandInfo.isCaseSensitive() ? check.equals(label) : check.equalsIgnoreCase(label);
	}
}
