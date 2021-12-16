package io.schark.pony.utils;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.JDAImpl;

import java.util.function.Function;

/**
 * @author Player_Schark
 */
public class PonyMappings {


	private static User matchUser(JDA jda, String snowflake) {
		long id = MiscUtil.parseSnowflake(snowflake);
		return jda.getUserById(id);
	}

	private static Role matchRole(JDA jda, String snowflake) {
		long id = MiscUtil.parseSnowflake(snowflake);
		return jda.getRoleById(id);
	}

	private static GuildChannel matchChannel(JDA jda, String snowflake) {
		long id = MiscUtil.parseSnowflake(snowflake);
		return jda.getGuildChannelById(id);
	}
	
	private static Emote matchEmote(JDA jda, String snowflake) {
		long id = MiscUtil.parseSnowflake(snowflake);
		return jda.getEmoteById(id);
	}
	
	public static Function<String, IMentionable> getMapping(JDA jda, Message.MentionType type) {
		return switch (type) {
			case USER -> snowflake->PonyMappings.matchUser(jda, snowflake);
			case ROLE -> snowflake->PonyMappings.matchRole(jda, snowflake);
			case CHANNEL -> snowflake->PonyMappings.matchChannel(jda, snowflake);
			case EMOTE -> snowflake->PonyMappings.matchEmote(jda, snowflake);
			default -> throw new IllegalStateException("Unexpected value: " + type);
		};
	}
}
