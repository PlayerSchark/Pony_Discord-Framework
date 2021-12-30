package io.schark.pony.core.feat.commands.command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

/**
 * @author Player_Schark
 */
public interface IPonyDiscordCommand extends IPonyCommand {

	InteractionHook getHook();
	@Override SlashCommandEvent getEvent();
	@Override Member getSender();
}
