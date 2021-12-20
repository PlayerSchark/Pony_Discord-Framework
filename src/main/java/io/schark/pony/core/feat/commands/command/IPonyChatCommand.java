package io.schark.pony.core.feat.commands.command;

import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

/**
 * @author Player_Schark
 */
public interface IPonyChatCommand extends IPonyCommand {
	boolean hasUserMention();
	boolean hasMemberMention();
	List<IMentionable> getMentions(Mention mention);
	Message getMessage();
}
