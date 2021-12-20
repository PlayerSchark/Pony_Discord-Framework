package io.schark.pony.core;

import io.schark.pony.exception.IdNotFoundException;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Player_Schark
 */
@Getter
public abstract class PonyBot {

	private String prefix;
	private String token;
	private long id;
	private JDA jda;

	public abstract void init(String[] args);

	public JDABuilder build() {
		JDABuilder builder = JDABuilder.create(this.token,
						GatewayIntent.GUILD_MEMBERS,
						GatewayIntent.GUILD_WEBHOOKS,
						GatewayIntent.GUILD_MESSAGES);

		builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
		builder.setBulkDeleteSplittingEnabled(false);
		builder.setCompression(Compression.NONE);
		builder.setActivity(this.getActivity());
		return builder;
	}

	private Activity getActivity() {
		return Activity.watching("Pony Twi-Fi");
	}


	public synchronized void beforeShutdown() {
		//does nothing but will be executed
	}

	public synchronized void afterShutdown() {
		//does nothing but will be executed
	}

	public void afterStart() {
		//does nothing but will be executed
	}

	private <T> T getById(long id, Function<Long, T> function, String type) {
		T obj = function.apply(id);
		if (obj == null) {
			throw new IdNotFoundException(id, type);
		}
		return obj;
	}

	public Guild getGuildById(Long guildId) {
		return this.getById(guildId, this.jda::getGuildById, "GuildId");
	}

	@NotNull public Set<Long> getGuildIds() {
		Set<Long> guildIds = new HashSet<>();
		List<Guild> guilds = this.jda.getGuilds();
		for (Guild guild : guilds) {
			guildIds.add(guild.getIdLong());
		}
		return guildIds;
	}
}
