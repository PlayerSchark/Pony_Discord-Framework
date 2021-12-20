package io.schark.pony.core.feat.commands.annotation.impl;

import io.schark.pony.core.feat.commands.executor.PonyCommandExecutable;

/**
 * @author Player_Schark
 */
public interface PonyAccessor {
	Long[] getIds(PonyCommandExecutable executor);
}
