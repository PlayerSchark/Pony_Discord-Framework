package io.schark.pony.core.feat.commands.command;

import com.google.common.base.Preconditions;
import io.schark.pony.core.feat.commands.in.PonyArg;
import io.schark.pony.core.feat.commands.in.PonyLabel;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

/**
 * @author Player_Schark
 */
public class PonyPrivateCommand extends PonyCommand {

	public PonyPrivateCommand(IMentionable sender, Message message, MessageChannel channel, PonyLabel label, List<PonyArg> args) {
		super(sender, message, channel, label, args);
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
