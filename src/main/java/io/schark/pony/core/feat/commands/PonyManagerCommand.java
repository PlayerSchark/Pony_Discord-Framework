package io.schark.pony.core.feat.commands;

import io.schark.pony.Pony;
import io.schark.pony.core.PonyManager;
import io.schark.pony.core.PonyManagerType;
import io.schark.pony.core.feat.PonyManagerListener;
import io.schark.pony.core.feat.commands.listener.PonyChatCommandListener;
import io.schark.pony.core.feat.commands.listener.PonyDiscordCommandListener;
import io.schark.pony.core.feat.commands.registry.PonyCommandRegistry;
import io.schark.pony.exception.CommandRegisterException;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Player_Schark
 */
@Getter
public class PonyManagerCommand extends PonyManager {

	private final PonyCommandRegistry registry = new PonyCommandRegistry();
	private String prefix;

	@Override public void init() {
		try {
			this.prefix = Pony.getInstance().getConfig().getPrefix();
			this.registerCommands();

			boolean noCommands = this.registry.isNoCommands();
			if (noCommands) {
				return;
			}
			PonyManagerListener listener = Pony.getInstance().getManager(PonyManagerType.LISTENER);
			if (listener != null) {
				listener.registerListener(new PonyChatCommandListener(this));
				listener.registerListener(new PonyDiscordCommandListener(this));
			}
		}
		catch (Exception e) {
			throw new CommandRegisterException(e);
		}
	}

	private void registerCommands() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Method method = this.registry.getClass().getDeclaredMethod("reflectExecutors");
		method.setAccessible(true);
		method.invoke(this.registry);
		method.setAccessible(false);
	}

	@Override public JDA getJda() {
		return null;
	}

	public String getPrefix() {
		return this.prefix;
	}
}
