package io.schark.pny.example;

import io.schark.pony.feat.commands.annotation.impl.PonyAccessor;
import io.schark.pony.feat.commands.PonyChatCommandExecutor;

/**
 * @author Player_Schark
 */
public class CommandAccessor implements PonyAccessor {

	@Override public Long[] getIds(PonyChatCommandExecutor executor) {
		return new Long[0];
	}
}
