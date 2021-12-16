package io.schark.pony.core.feat.commands.annotation.impl;

import io.schark.pony.core.feat.commands.executor.PonyCommandExecutor;

/**
 * @author Player_Schark
 */
public interface PonyAccessor {
	Long[] getIds(PonyCommandExecutor executor);
}
