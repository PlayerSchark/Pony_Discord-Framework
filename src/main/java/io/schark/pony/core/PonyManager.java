package io.schark.pony.core;

import io.schark.pony.feat.commands.registry.CommandRegistry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;

/**
 * @author Player_Schark
 */
@Getter
@RequiredArgsConstructor
public class PonyManager {

	private final JDA jda;
	private final CommandRegistry registry;
}
