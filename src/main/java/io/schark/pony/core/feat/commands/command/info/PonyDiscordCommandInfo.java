package io.schark.pony.core.feat.commands.command.info;

import io.schark.pony.core.feat.commands.annotation.impl.PonyRunnable;
import io.schark.pony.core.feat.commands.executor.PonyDiscordCommandExecutable;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.Set;
import java.util.function.BiFunction;

/**
 * @author Player_Schark
 */
public class PonyDiscordCommandInfo<E extends PonyDiscordCommandExecutable> extends PonyCommandInfo<E> {

	private boolean sendThinking;
	private boolean ephemeral;

	public PonyDiscordCommandInfo(E executable, Set<Long> allowedRoles, Set<Long> allowedUsers, Set<Long> allowedGuilds, Set<Long> allowedChannels, boolean botUsable,
					boolean caseSensitive,
					boolean blacklisted,
					boolean sendThinking,
					boolean ephemeral,
					PonyRunnable noRole,
					PonyRunnable noUser,
					PonyRunnable noGuild,
					PonyRunnable noChannel,
					String noRoleMessage,
					String noUserMessage,
					String noGuildMessage,
					String noChannelMessage
	) {
		super(executable, allowedRoles, allowedUsers, allowedGuilds, allowedChannels, botUsable, false, caseSensitive, blacklisted, noRole, noUser, noGuild, noChannel, noRoleMessage, noUserMessage,
						noGuildMessage, noChannelMessage);
		this.sendThinking = sendThinking;
		this.ephemeral = ephemeral;
	}

	@Override
	public E getExecutable() {
		return super.getExecutable();
	}

	public boolean hasAccess(SlashCommandEvent e) {
		BiFunction<Event, String, RestAction> noAccessFunction = (ev, msg) -> e.getChannel().sendMessage(msg);
		return super.hasAccess(e, noAccessFunction, e.getUser(), e.getMember(), e.getChannel());
	}
}
