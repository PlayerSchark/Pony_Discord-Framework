package io.schark.pony.core.feat.commands.registry.info;

import io.schark.pony.core.feat.commands.executor.input.PonyChatCommandExecutable;
import io.schark.pony.core.feat.commands.registry.wrapper.PonyAnnotationWrapper;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * @author Player_Schark
 */
public class PonyChatCommandInfo extends PonyCommandInfo<PonyChatCommandExecutable> {

	public PonyChatCommandInfo(PonyChatCommandExecutable executable, PonyAnnotationWrapper wrapper, boolean botUsable, boolean sendTyping, boolean caseSensitive, boolean blacklisted) {
		super(executable, wrapper, botUsable, sendTyping, caseSensitive, blacklisted);
	}

	public boolean hasAccess(Message message, @Nullable Member member) {
		Function<String, RestAction> noAccessFunction = msg -> message.getChannel().sendMessage(msg);
		return super.hasAccess(noAccessFunction, message.getAuthor(), member, message.getChannel());
	}
}
