package io.schark.pony.core.feat.commands.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Message;

/**
 * @author Player_Schark
 */
@Getter
@RequiredArgsConstructor
public enum Mention {

	USER(Message.MentionType.USER),
	MEMBER(Message.MentionType.USER),
	ROLE(Message.MentionType.ROLE),
	CHANNEL(Message.MentionType.CHANNEL),
	EMOTE(Message.MentionType.EMOTE),
	HERE(Message.MentionType.HERE),
	EVERYONE(Message.MentionType.EVERYONE);

	private final Message.MentionType type;
}
