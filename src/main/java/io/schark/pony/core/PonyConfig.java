package io.schark.pony.core;

import io.schark.pony.Pony;
import io.schark.pony.utils.PonyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Player_Schark
 */
public class PonyConfig extends Properties {

	public String getPonyBotMain() {
		return this.getProperty("main");
	}

	public String getDiscordTokenPath() {
		return this.getProperty("token");
	}

	public long getId() {
		String rawId = (String) this.get("id");
		return Long.parseLong(rawId);
	}

	public String getCommandPackage() {
		return this.getProperty("commands");
	}

	public int getDiscordTokenLine() {
		return Integer.parseInt(this.getProperty("token.line")) - 1;
	}

	public String getSpotifyTokenPath() {
		return this.getProperty("token.spotify");
	}

	public int getSpotifyAccessTokenLine() {
		return Integer.parseInt(this.getProperty("token.spotify.line")) - 1;
	}

	public String getPrefix() {
		return this.getProperty("prefix");
	}

	public boolean unregisterCommands() {
		return Boolean.parseBoolean(this.getProperty("unregisterCommands"));
	}

	public String getSpotifyAccessToken() throws IOException {
		String path = this.getSpotifyTokenPath();
		int line = this.getSpotifyAccessTokenLine();
		return this.getToken(path, line);
	}

	public String getDiscordToken() throws IOException {
		String path = this.getDiscordTokenPath();
		int line = this.getDiscordTokenLine();
		return this.getToken(path, line);
	}

	private String getToken(String path, int line) throws IOException {

		InputStream input = Pony.class.getResourceAsStream("/" + path);
		String[] fileContent = PonyUtils.getFileContent(input).split("\n");
		return fileContent[line];
	}
}
