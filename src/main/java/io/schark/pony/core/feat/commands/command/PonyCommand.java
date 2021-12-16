package io.schark.pony.core.feat.commands.command;
import io.schark.pony.core.feat.commands.in.PonyArg;
import io.schark.pony.core.feat.commands.in.PonyLabel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.*;

import java.util.List;

/**
 * @author Player_Schark
 */
@Getter
@RequiredArgsConstructor
public abstract class PonyCommand {
	private final IMentionable sender;
	private final Message message;
	private final MessageChannel channel;
	private final PonyLabel label;
	private final List<PonyArg> arguments;

	public List<IMentionable> getMentions(Mention mention) {
		return this.message.getMentions(mention.getType());
	}

	public boolean hasUserMention() {
		return !this.message.getMentionedUsers().isEmpty();
	}

	public boolean hasMemberMention() {
		return !this.message.getMentionedMembers().isEmpty();
	}

	public IMentionable getFirstMentionedOrSender() {
		for (PonyArg arg : this.arguments) {
			if (arg.hasMention()) {
				IMentionable iMentionable = arg.toMentions().get(0);
				if (iMentionable instanceof Member || iMentionable instanceof User) {
					return iMentionable;
				}
			}
		}
		return this.sender;
	}

}
