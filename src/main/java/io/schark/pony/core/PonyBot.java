package io.schark.pony.core;

import io.schark.pony.Pony;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;

/**
 * @author Player_Schark
 */
@Getter
public abstract class PonyBot {

	private String token;
	private long id;
	private JDA jda;

	public abstract void init(String[] args);

	public JDA build() throws LoginException {
		JDABuilder builder = JDABuilder.createDefault(this.token);

		builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
		builder.setBulkDeleteSplittingEnabled(false);
		builder.setCompression(Compression.NONE);
		builder.setActivity(this.getActivity());
		builder.addEventListeners(this.getListeners());
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
}
