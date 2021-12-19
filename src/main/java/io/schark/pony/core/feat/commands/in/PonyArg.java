package io.schark.pony.core.feat.commands.in;

import io.schark.pony.utils.PonyMappings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * @author Player_Schark
 */
@Getter
@RequiredArgsConstructor
public abstract class PonyArg<T> {

	private final JDA jda;
	private final String contentAsString;
	private final T content;

	public boolean hasMention() {
		for (Message.MentionType type : Message.MentionType.values()) {
			boolean matches = type.getPattern().matcher(this.contentAsString).matches();
			if (matches) {
				return true;
			}
		}
		return false;
	}

	public List<IMentionable> toMentions() {
		List<IMentionable> mentions = new ArrayList<>();

		for (Message.MentionType type : Message.MentionType.values()) {
			List<IMentionable> foundMentions = this.handleMentions(type, PonyMappings.getMapping(this.getJda(), type));
			mentions.addAll(foundMentions);
		}
		return Collections.unmodifiableList(mentions);
	}

	private <M extends IMentionable> List<M> handleMentions(Message.MentionType type, Function<String, M> mapper) {
		Matcher matcher = type.getPattern().matcher(this.getContentAsString());
		return matcher.results().map(MatchResult::group).map(mapper).collect(Collectors.toList());
	}

	public abstract PonyArg<String> getAsString();
}
