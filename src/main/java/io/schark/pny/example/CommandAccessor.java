package io.schark.pny.example;

import io.schark.pony.core.feat.commands.annotation.impl.PonyAccessor;
import io.schark.pony.core.feat.commands.executor.PonyCommandExecutable;

/**
 * @author Player_Schark
 */
public class CommandAccessor implements PonyAccessor {
	@Override public Long[] getIds(PonyCommandExecutable executor) {
		return new Long[0];
	}
}
