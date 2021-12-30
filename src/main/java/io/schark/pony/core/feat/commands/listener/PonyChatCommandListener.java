package io.schark.pony.core.feat.commands.listener;

import io.schark.pony.Pony;
import io.schark.pony.core.feat.commands.PonyManagerCommand;
import io.schark.pony.core.feat.commands.command.PonyChatCommand;
import io.schark.pony.core.feat.commands.command.PonyPrivateChatCommand;
import io.schark.pony.core.feat.commands.command.PonyPublicChatCommand;
import io.schark.pony.core.feat.commands.executor.input.PonyChatCommandExecutor;
import io.schark.pony.core.feat.commands.in.PonyArg;
import io.schark.pony.core.feat.commands.in.PonyChatArg;
import io.schark.pony.core.feat.commands.in.PonyChatLabel;
import io.schark.pony.core.feat.commands.registry.PonyCommandRegistry;
import io.schark.pony.core.feat.commands.registry.info.PonyChatCommandInfo;
import io.schark.pony.core.feat.commands.registry.info.PonyCommandInfo;
import io.schark.pony.exception.CommandProcessException;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Player_Schark
 */
@RequiredArgsConstructor
public class PonyChatCommandListener extends ListenerAdapter {

	private final PonyManagerCommand manager;

	@Override
	public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent e) {
		this.onMessageReceived(e, e.getMessage(), e.getAuthor(), null);
	}

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent e) {
		this.onMessageReceived(e, e.getMessage(), e.getAuthor(), e.getMember());
	}

	private void onMessageReceived(Event e, Message message, User author, @Nullable Member member) {
		if (author.getIdLong() == Pony.getInstance().getPonyBot().getId()) {
			return;
		}

		if (!message.getContentRaw().startsWith(this.manager.getPrefix())) {
			return;
		}

		String[] splitted = this.splitMessage(message);
		PonyChatLabel label = this.getLabel(splitted[0]);

		PonyCommandRegistry registry = this.manager.getRegistry();
		if (!registry.isChatCommandRegistered(label.getContent())) {
			return;
		}

		PonyChatCommandExecutor executor = (PonyChatCommandExecutor) registry.getChatCommand(label);

		if (executor == null) {
			throw new CommandProcessException();
		}

		if (!executor.isGuildCommand()) {
			return;
		}

		PonyChatCommandInfo info = registry.getChatCommandInfo(executor);
		if (!info.hasAccess(message, member)) {
			return;
		}

		boolean guildCommand = executor.isGuildCommand();
		PonyChatCommand command = this.parseCommandMessage(e, guildCommand, splitted, message, label);
		this.execute(executor, command);
	}

	private void execute(PonyChatCommandExecutor executor, PonyChatCommand command) {
		MessageChannel channel = command.getMessage().getChannel();
		PonyCommandInfo commandInfo = this.manager.getRegistry().getChatCommandInfo(executor);
		if (commandInfo.isSendTyping()) {
			channel.sendTyping().queue();
		}

		String answer = executor.ponyExecute(command);

		if (answer != null && !answer.isEmpty()) {
			channel.sendMessage(answer).queue();
		}
	}

	private String[] splitMessage(Message message) {
		String content = message.getContentRaw().trim().replaceAll(" +", " ");
		String commandMessage = content.substring(this.manager.getPrefix().length());
		return commandMessage.split(" ");
	}

	private PonyChatLabel getLabel(String rawLabel) {
		JDA jda = this.manager.getJda();
		return new PonyChatLabel(jda, rawLabel);
	}

	private PonyChatCommand parseCommandMessage(Event e, boolean guildCommand, String[] splitted, Message message, PonyChatLabel label) {
		JDA jda = this.manager.getJda();
		IMentionable sender = this.getSender(message);
		MessageChannel channel = message.getChannel();
		List<PonyArg<String>> finalArgs = this.packArgs(jda, splitted);

		return this.getCommand(e, guildCommand, sender, message, channel, label, finalArgs);
	}

	private IMentionable getSender(Message message) {
		return message.isFromGuild() ? message.getMember() : message.getAuthor();
	}

	private List<PonyArg<String>> packArgs(JDA jda, String[] splitted) {
		List<PonyArg<String>> ponyArgs = new ArrayList<>();
		List<String> args = new ArrayList<>(Arrays.asList(splitted).subList(1, splitted.length));
		for (String arg : args) {
			ponyArgs.add(new PonyChatArg(jda, arg));
		}
		return Collections.unmodifiableList(ponyArgs);
	}

	private PonyChatCommand getCommand(Event e, boolean guildCommand, IMentionable sender, Message message, MessageChannel channel, PonyChatLabel label, List<PonyArg<String>> args) {
		return guildCommand ? new PonyPublicChatCommand(((MessageReceivedEvent) e), sender, message, channel, label, args) :
						new PonyPrivateChatCommand(((PrivateMessageReceivedEvent) e), sender, message, channel, label, args);
	}
}
