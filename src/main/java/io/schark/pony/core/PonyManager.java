package io.schark.pony.core;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;

/**
 * @author Player_Schark
 */
@Getter
public abstract class PonyManager {

	public abstract void init();
	@Setter private boolean enabled;
	@Setter private JDA jda;
}
