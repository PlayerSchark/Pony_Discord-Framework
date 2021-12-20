package io.schark.pony.core.feat.commands.command;

import io.schark.pony.core.feat.commands.in.PonyArg;
import io.schark.pony.core.feat.commands.in.PonyChatLabel;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.Event;

import java.util.List;

/**
 * @author Player_Schark
 */
public interface IPonyCommand {

	IMentionable getFirstMentionedOrSender();
	Event getEvent();
	IMentionable getSender();
	MessageChannel getChannel();
	PonyChatLabel getLabel();
	<T> PonyArg<T> getArgument(int i);
	List<? extends PonyArg<?>> getArguments();
	List<? extends PonyArg<String>> getStringArguments();
	List<String> getRawArguments();
}
