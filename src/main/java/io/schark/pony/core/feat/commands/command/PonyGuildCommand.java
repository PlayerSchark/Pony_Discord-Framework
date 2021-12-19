package io.schark.pony.core.feat.commands.command;

import io.schark.pony.core.feat.commands.in.PonyChatLabel;
import io.schark.pony.core.feat.commands.in.PonyGuildArg;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.List;

/**
 * @author Player_Schark
 */
@Getter
public class PonyGuildCommand extends PonyCommandBase<SlashCommandEvent, PonyGuildArg<?>> implements PonyCommand {

	private final InteractionHook hook;

	public PonyGuildCommand(SlashCommandEvent e, Member sender, InteractionHook hook, MessageChannel channel,
					PonyChatLabel label, List<PonyGuildArg<?>> args) {
		super(e, sender, channel, label, args);
		this.hook = hook;
	}

	@Override
	public Member getSender() {
		return (Member) super.getSender();
	}
}
