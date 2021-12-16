package io.schark.pony.core.feat;

import io.schark.pony.core.PonyManager;
import io.schark.pony.core.feat.commands.listener.PonyCommandListener;
import lombok.Getter;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Player_Schark
 */
@Getter
public class PonyManagerListener extends PonyManager {

	private List<ListenerAdapter> listeners = new ArrayList<>();

	@Override public void init() {
		//noting to do here
	}

	public void registerListener(PonyCommandListener ponyCommandListener) {
		this.listeners.add(ponyCommandListener);
	}

	public ListenerAdapter[] getRegisterableListeners() {
		return this.listeners.toArray(new ListenerAdapter[0]);
	}
}
