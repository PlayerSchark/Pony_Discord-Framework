package io.schark.pony.core.feat.commands.registry.info;

import io.schark.pony.core.feat.commands.executor.input.PonyDiscordCommandExecutable;
import io.schark.pony.core.feat.commands.registry.wrapper.PonyAnnotationWrapper;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.function.Function;

/**
 * @author Player_Schark
 */
public class PonyDiscordCommandInfo<E extends PonyDiscordCommandExecutable> extends PonyCommandInfo<E> {

	private boolean sendThinking;
	private boolean ephemeral;

	public PonyDiscordCommandInfo(E executable, PonyAnnotationWrapper wrapper, boolean botUsable,
                                boolean caseSensitive,
                                boolean blacklisted,
                                boolean sendThinking,
                                boolean ephemeral) {
		super(executable, wrapper, botUsable, false, caseSensitive, blacklisted);
		this.sendThinking = sendThinking;
		this.ephemeral = ephemeral;
	}

	@Override
	public E getExecutable() {
		return super.getExecutable();
	}

	public boolean hasAccess(SlashCommandEvent e) {
		Function<String, RestAction> noAccessFunction = msg -> e.getChannel().sendMessage(msg);
		return super.hasAccess(noAccessFunction, e.getUser(), e.getMember(), e.getChannel());
	}
}
