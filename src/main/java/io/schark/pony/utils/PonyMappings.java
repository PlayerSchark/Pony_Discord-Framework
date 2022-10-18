package io.schark.pony.utils;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.utils.MiscUtil;

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
	
	public static Function<String, IMentionable> getMapping(JDA jda, Message.MentionType type) {
		return switch (type) {
			case USER -> snowflake->PonyMappings.matchUser(jda, snowflake);
			case ROLE -> snowflake->PonyMappings.matchRole(jda, snowflake);
			case CHANNEL -> snowflake->PonyMappings.matchChannel(jda, snowflake);
			default -> throw new IllegalStateException("Unexpected value: " + type);
		};
	}
}
