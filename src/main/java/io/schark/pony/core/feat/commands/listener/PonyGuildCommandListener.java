package io.schark.pony.core.feat.commands.listener;

import io.schark.pony.core.feat.commands.PonyManagerCommand;
import io.schark.pony.core.feat.commands.command.PonyGuildCommand;
import io.schark.pony.core.feat.commands.executor.PonyGuildCommandExecutable;
import io.schark.pony.core.feat.commands.in.PonyChatLabel;
import io.schark.pony.core.feat.commands.in.PonyGuildArg;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Player_Schark
 */
@RequiredArgsConstructor
public class PonyGuildCommandListener extends ListenerAdapter {


	private final PonyManagerCommand manager;

	@Override
	public void onSlashCommand(SlashCommandEvent e) {
		PonyGuildCommandExecutable executor = this.manager.getRegistry().getGuildCommand(e.getName());
		Member member = e.getMember();
		InteractionHook hook = e.getHook();
		MessageChannel channel = e.getChannel();
		PonyChatLabel label = new PonyChatLabel(e.getJDA(), e.getName());
		List<PonyGuildArg<?>> args = this.parseOptions(e.getOptions());
		PonyGuildCommand command =  new PonyGuildCommand(e, member, hook, channel, label, args);
		String result = executor.ponyExecute(command);
		executor.afterExecute(command, result);
	}

	private List<PonyGuildArg<?>> parseOptions(List<OptionMapping> options) {
		List<PonyGuildArg<?>> result = new ArrayList<>();
		for (OptionMapping option : options) {
			PonyGuildArg<?> arg = this.parseOption(option);
			result.add(arg);
		}
		return result;
	}

	private PonyGuildArg<?> parseOption(OptionMapping mapping) {
		OptionType type = mapping.getType();
		JDA jda = this.manager.getJda();
		String content = mapping.getAsString();
		return switch (type) {
			case BOOLEAN -> new PonyGuildArg<>(jda, mapping, content, mapping.getAsBoolean());
			case INTEGER -> new PonyGuildArg<>(jda, mapping, content, mapping.getAsLong());
			case MENTIONABLE, CHANNEL -> new PonyGuildArg<>(jda, mapping, content, mapping.getAsMentionable());
			case NUMBER -> new PonyGuildArg<>(jda, mapping, content, mapping.getAsDouble());
			case ROLE -> new PonyGuildArg<>(jda, mapping, content, mapping.getAsRole());
			case STRING, SUB_COMMAND, SUB_COMMAND_GROUP -> new PonyGuildArg<>(jda, mapping, content, content);
			case UNKNOWN -> new PonyGuildArg<>(jda, mapping, content, mapping);
			case USER -> new PonyGuildArg<>(jda, mapping, content, mapping.getAsMember() == null ? mapping.getAsUser() : mapping.getAsMember());
		};
	}
}
