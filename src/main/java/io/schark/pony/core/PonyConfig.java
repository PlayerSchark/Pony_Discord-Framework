package io.schark.pony.core;

import java.util.Properties;

/**
 * @author Player_Schark
 */
public class PonyConfig extends Properties {

	public String getPonyBotMain() {
		return this.getProperty("main");
	}

	public String getTokenPath() {
		return this.getProperty("token");
	}

	public long getId() {
		String rawId = (String) this.get("id");
		return Long.parseLong(rawId);
	}

	public String getCommandPackage() {
		return this.getProperty("commands");
	}
}
