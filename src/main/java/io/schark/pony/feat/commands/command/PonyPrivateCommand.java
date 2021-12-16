package io.schark.pony.feat.commands.command;

import com.google.common.base.Preconditions;
import io.schark.pony.feat.commands.in.PonyArg;
import io.schark.pony.feat.commands.in.PonyLabel;
import net.dv8tion.jda.api.entities.*;

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
