package io.schark.pony.core.feat.commands.command;

import com.google.common.base.Preconditions;
import io.schark.pony.core.feat.commands.in.PonyArg;
import io.schark.pony.core.feat.commands.in.PonyChatLabel;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

/**
 * @author Player_Schark
 */
public class PonyPublicChatCommand extends PonyChatCommand implements IPonyGuildable {

	public PonyPublicChatCommand(MessageReceivedEvent e, IMentionable sender, Message message, MessageChannel channel, PonyChatLabel label, List<PonyArg<String>> args) {
		super(e, sender, message, channel, label, args);
	}

	@Override public Member getSender() {
		return (Member) super.getSender();
	}

	@Override
	public List<IMentionable> getMentions(Mention mention) {
		Preconditions.checkArgument(mention != Mention.USER);
		return super.getMentions(mention);
	}

	@Override
	public Member getFirstMentionedOrSender() {
		return (Member) super.getFirstMentionedOrSender();
	}

	@Override public Guild getGuild() {
		return this.getMessage().getGuild();
	}
}
