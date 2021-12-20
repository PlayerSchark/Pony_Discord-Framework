package io.schark.pony.core.feat.commands.in;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

/**
 * @author Player_Schark
 */
@Getter
public class PonyGuildArg<T> extends PonyArg<T> {

	private final OptionMapping mapping;

	public PonyGuildArg(JDA jda, OptionMapping mapping, String contentAsString, T content) {
		super(jda, contentAsString, content);
		this.mapping = mapping;
	}

	@Override public PonyArg<String> getAsString() {
		return new PonyGuildArg<>(this.getJda(), this.getMapping(), this.getContentAsString(), this.getContentAsString());
	}
}
