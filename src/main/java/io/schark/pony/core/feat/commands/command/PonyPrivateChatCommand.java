package io.schark.pony.core.feat.commands.command;

import com.google.common.base.Preconditions;
import io.schark.pony.core.feat.commands.in.PonyArg;
import io.schark.pony.core.feat.commands.in.PonyChatLabel;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

/**
 * @author Player_Schark
 */
public class PonyPrivateChatCommand extends PonyChatCommand {

	public PonyPrivateChatCommand(MessageReceivedEvent e, IMentionable sender, Message message, MessageChannel channel, PonyChatLabel label, List<PonyArg<String>> args) {
		super(e, sender, message, channel, label, args);
	}

	@Override
	public List<IMentionable> getMentions(Mention mention) {
		Preconditions.checkArgument(mention != Mention.MEMBER);
		return super.getMentions(mention);
	}

	@Override
	public User getFirstMentionedOrSender() {
		return (User) super.getFirstMentionedOrSender();
	}
}
