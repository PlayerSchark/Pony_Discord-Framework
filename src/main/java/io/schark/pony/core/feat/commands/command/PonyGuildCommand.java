package io.schark.pony.core.feat.commands.command;

import io.schark.pony.core.feat.commands.in.PonyArg;
import io.schark.pony.core.feat.commands.in.PonyLabel;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.List;

/**
 * @author Player_Schark
 */
public class PonyGuildCommand extends PonyPublicCommand {

	public PonyGuildCommand(IMentionable sender, Message message, MessageChannel channel,
					PonyLabel label, List<PonyArg> args) {
		super(sender, message, channel, label, args);
	}
}
