package io.schark.pony.feat.commands.listener;

import io.schark.pony.Pony;
import io.schark.pony.feat.commands.command.PonyCommand;
import io.schark.pony.feat.commands.command.PonyPublicCommand;
import io.schark.pony.feat.commands.command.PonyPrivateCommand;
import io.schark.pony.feat.commands.in.PonyArg;
import io.schark.pony.feat.commands.in.PonyLabel;
import io.schark.pony.feat.commands.command.CommandInfo;
import io.schark.pony.core.PonyManager;
import io.schark.pony.feat.commands.PonyChatCommandExecutor;
import io.schark.pony.exception.CommandProcessException;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.*;

/**
 * @author Player_Schark
 */
@RequiredArgsConstructor
public class PonyCommandListener extends ListenerAdapter {

	private final PonyManager manager;

	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		if (e.getAuthor().getIdLong() == Pony.getPonyBot().getId()) {
			return;
		}

		Message message = e.getMessage();
		String[] splitted = this.splitMessage(message);
		PonyLabel label = this.getLabel(splitted[0]);

		if (!this.manager.getRegistry().isRegistered(label.getContentRaw())) {
			return;
		}

		PonyChatCommandExecutor executor = this.manager.getRegistry().get(label);

		if (executor == null) {
			throw  new CommandProcessException();
		}

		if (!this.manager.getRegistry().hasAccess(e.getMessage(), executor)) {
			return;
		}

		boolean guildCommand = executor.isGuildCommand();
		PonyCommand command = this.parseCommandMessage(guildCommand, splitted, message, label);
		this.execute(executor, command);
	}

	private void execute(PonyChatCommandExecutor executor, PonyCommand command) {
		MessageChannel channel = command.getMessage().getChannel();
		CommandInfo commandInfo = this.manager.getRegistry().getcommandInfo(executor);
		if (commandInfo.isSendTyping()) {
			channel.sendTyping().queue();
		}

		String answer = executor.execute(command);

		if (answer != null) {
			channel.sendMessage(answer).queue();
		}
	}

	private String[] splitMessage(Message message) {
		String content = message.getContentRaw().trim().replaceAll(" +", " ");
		String commandMessage = content.substring("!".length());
		return commandMessage.split(" ");
	}

	private PonyLabel getLabel(String rawLabel) {
		JDA jda = this.manager.getJda();
		return new PonyLabel(jda, rawLabel);
	}

	private PonyCommand parseCommandMessage(boolean guildCommand, String[] splitted, Message message, PonyLabel label) {
		JDA jda = this.manager.getJda();
		IMentionable sender = this.getSender(message);
		MessageChannel channel = message.getChannel();
		List<PonyArg> finalArgs = this.packArgs(jda, splitted);

		return this.getCommand(guildCommand, sender, message, channel, label, finalArgs);
	}

	private IMentionable getSender(Message message) {
		return message.isFromGuild() ? message.getMember() : message.getAuthor();
	}

	private List<PonyArg> packArgs(JDA jda, String[] splitted) {
		List<PonyArg> ponyArgs = new ArrayList<>();
		List<String> args = new ArrayList<>(Arrays.asList(splitted).subList(1, splitted.length));
		for (String arg : args) {
			ponyArgs.add(new PonyArg(jda, arg));
		}
		return Collections.unmodifiableList(ponyArgs);
	}

	private PonyCommand getCommand(boolean guildCommand, IMentionable sender, Message message, MessageChannel channel, PonyLabel label, List<PonyArg> args) {
		return guildCommand ? new PonyPublicCommand(sender, message, channel, label, args) :
						new PonyPrivateCommand(sender, message, channel, label, args);
	}
}
