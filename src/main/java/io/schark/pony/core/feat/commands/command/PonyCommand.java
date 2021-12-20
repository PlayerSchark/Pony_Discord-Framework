package io.schark.pony.core.feat.commands.command;

import io.schark.pony.core.feat.commands.in.PonyArg;
import io.schark.pony.core.feat.commands.in.PonyChatLabel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Player_Schark
 */
@Getter
@RequiredArgsConstructor
public abstract class PonyCommand<E extends Event, A extends PonyArg<?>> implements IPonyCommand {

	private final E event;
	private final IMentionable sender;
	private final MessageChannel channel;
	private final PonyChatLabel label;
	private final List<A> arguments;

	public IMentionable getFirstMentionedOrSender() {
		for (A arg : this.arguments) {
			if (arg.hasMention()) {
				List<IMentionable> list = arg.toMentions();
				IMentionable iMentionable = list.get(0);
				if (iMentionable instanceof Member || iMentionable instanceof User) {
					return iMentionable;
				}
			}
		}
		return this.sender;
	}

	public <T> PonyArg<T> getArgument(int i) {
		//noinspection unchecked
		return (PonyArg<T>) this.getArguments().get(i);
	}

	public PonyArg<String> getStringArgument(int i) {
		return this.getArgument(i).getAsString();
	}

	public List<? extends PonyArg<String>> getStringArguments() {
		List<PonyArg<String>> result = new ArrayList<>();
		for (A argument : this.arguments) {
			result.add(argument.getAsString());
		}
		return Collections.unmodifiableList(result);
	}

	public List<String> getRawArguments() {
		List<String> result = new ArrayList<>();
		for (A argument : this.arguments) {
			result.add(argument.getContentAsString());
		}
		return Collections.unmodifiableList(result);
	}
}
