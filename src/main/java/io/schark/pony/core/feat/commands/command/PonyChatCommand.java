package io.schark.pony.core.feat.commands.command;

import io.schark.pony.core.feat.commands.in.PonyArg;
import io.schark.pony.core.feat.commands.in.PonyChatLabel;
import lombok.Getter;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.Event;

import java.util.List;

/**
 * @author Player_Schark
 */
@Getter
public class PonyChatCommand extends PonyCommand<Event, PonyArg<String>> implements IPonyChatCommand {

	private final Message message;

	public PonyChatCommand(Event e, IMentionable sender, Message message, MessageChannel channel, PonyChatLabel label,
					List<PonyArg<String>> arguments) {
		super(e, sender, channel, label, arguments);
		this.message = message;
	}

	public List<IMentionable> getMentions(Mention mention) {
		return this.message.getMentions(mention.getType());
	}

	public boolean hasUserMention() {
		return !this.message.getMentionedUsers().isEmpty();
	}

	public boolean hasMemberMention() {
		return !this.message.getMentionedMembers().isEmpty();
	}
}
