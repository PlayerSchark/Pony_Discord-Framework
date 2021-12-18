package io.schark.pony.core;

import io.schark.pony.Pony;
import io.schark.pony.exception.IdNotFoundException;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
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

	public JDA build() throws LoginException {
		JDABuilder builder = JDABuilder.create(this.token,
						GatewayIntent.GUILD_MEMBERS,
						GatewayIntent.GUILD_WEBHOOKS,
						GatewayIntent.GUILD_MESSAGES);

		builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
		builder.setBulkDeleteSplittingEnabled(false);
		builder.setCompression(Compression.NONE);
		builder.setActivity(this.getActivity());
		Object[] listeners = this.getListeners();
		builder.addEventListeners(listeners);
		return this.jda = builder.build();
	}

	private Object[] getListeners() {
		return Pony.getInstance().getManager(PonyManagerType.LISTENER).getRegisterableListeners();
	}

	private Activity getActivity() {
		return Activity.watching("Pony Twi-Fi");
	}


	public void beforeShutdown() {
		//does nothing but will be executed
	}

	public void afterShutdown() {
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
}
