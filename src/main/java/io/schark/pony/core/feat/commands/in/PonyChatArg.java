package io.schark.pony.core.feat.commands.in;

import net.dv8tion.jda.api.JDA;

/**
 * @author Player_Schark
 */
public class PonyChatArg extends PonyArg<String> {

	public PonyChatArg(JDA jda, String content) {
		super(jda, content, content);
	}

	@Override public PonyArg<String> getAsString() {
		return this;
	}
}
