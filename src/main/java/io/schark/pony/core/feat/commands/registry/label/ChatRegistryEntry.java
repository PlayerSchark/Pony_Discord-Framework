package io.schark.pony.core.feat.commands.registry.label;

import io.schark.pony.core.feat.commands.command.info.PonyChatCommandInfo;
import io.schark.pony.core.feat.commands.executor.PonyChatCommandExecutable;
import lombok.Getter;

import java.util.Set;

/**
 * @author Player_Schark
 */
@Getter
public class ChatRegistryEntry extends RegistryEntry<PonyChatCommandExecutable, PonyChatCommandInfo> {

	private final Set<String> aliases;

	public ChatRegistryEntry(String rawLabel, Set<String> aliases, PonyChatCommandInfo commandInfo) {
		super(rawLabel, commandInfo);
		this.aliases = aliases;
	}

	@Override
	public boolean matches(String label) {
		boolean matchesRaw = super.matches(this.getRawLabel(), label);
		if (matchesRaw) {
			return true;
		}
		for (String alias : this.aliases) {
			if (super.matches(alias, label)) {
				return true;
			}
		}
		return false;
	}
}
