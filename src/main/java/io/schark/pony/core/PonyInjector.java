package io.schark.pony.core;

import io.schark.pony.exception.PonyStartException;
import io.schark.pony.utils.PonyUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;

import java.io.IOException;

/**
 * @author Player_Schark
 */
@Getter
@RequiredArgsConstructor
public class PonyInjector {

	private final PonyBot ponyBot;
	private final PonyConfig config;

	public void injectBot() throws IOException, NoSuchFieldException, IllegalAccessException, PonyStartException {
		this.injectToken();
		this.injectId();
		this.injectPrefix();
	}

	private void injectToken() throws IOException, IllegalAccessException, NoSuchFieldException {
		String token = this.getConfig().getDiscordToken();
		PonyUtils.setValue(this.ponyBot, "token", token, PonyBot.class);
	}


	private void injectId() throws NoSuchFieldException, IllegalAccessException, PonyStartException {
		long id;
		try {
			id = this.config.getId();
		} catch (NumberFormatException ex) {
			throw new PonyStartException("Bot id could not be read");
		}
		PonyUtils.setValue(this.ponyBot, "id", id, PonyBot.class);
	}

	private void injectPrefix() throws IllegalAccessException, NoSuchFieldException {
		String prefix = this.config.getPrefix();
		PonyUtils.setValue(this.ponyBot, "prefix", prefix, PonyBot.class);
	}

	public void injectJda(JDA jda) throws NoSuchFieldException, IllegalAccessException {
		PonyUtils.setValue(this.ponyBot, "jda", jda, PonyBot.class);
	}
}
