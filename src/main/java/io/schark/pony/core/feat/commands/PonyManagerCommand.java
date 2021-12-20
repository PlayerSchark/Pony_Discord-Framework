package io.schark.pony.core.feat.commands;

import io.schark.pony.Pony;
import io.schark.pony.core.PonyManager;
import io.schark.pony.core.PonyManagerType;
import io.schark.pony.core.feat.commands.listener.PonyCommandListener;
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

	@Override public void init() {
		try {
			this.registerCommands();

			boolean noCommands = this.registry.isNoCommands();
			if (noCommands) {
				return;
			}
			Pony.getInstance().getManager(PonyManagerType.LISTENER).registerListener(new PonyCommandListener(this));
		}
		catch (Exception e) {
			throw new CommandRegisterException(e);
		}
	}

	private void registerCommands() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Method method = this.registry.getClass().getDeclaredMethod("reflectCommands");
		method.setAccessible(true);
		method.invoke(this.registry);
		method.setAccessible(false);
	}

	@Override public JDA getJda() {
		return null;
	}

	public String getPrefix() {
		return "!";
	}
}
