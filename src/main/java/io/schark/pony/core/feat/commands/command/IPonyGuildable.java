package io.schark.pony.core.feat.commands.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

/**
 * @author Player_Schark
 */
public interface IPonyGuildable {
	Member getSender();

	Member getFirstMentionedOrSender();

	Guild getGuild();
}
