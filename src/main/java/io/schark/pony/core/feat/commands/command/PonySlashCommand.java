package io.schark.pony.core.feat.commands.command;

import io.schark.pony.core.feat.commands.in.PonyChatLabel;
import io.schark.pony.core.feat.commands.in.PonyGuildArg;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.List;

/**
 * @author Player_Schark
 */
public class PonySlashCommand extends PonyDiscordCommand implements IPonyGuildable {

	public PonySlashCommand(SlashCommandEvent e, Member sender, InteractionHook hook,
					MessageChannel channel, PonyChatLabel label,
					List<PonyGuildArg<?>> args) {
		super(e, sender, hook, channel, label, args);
	}
}
