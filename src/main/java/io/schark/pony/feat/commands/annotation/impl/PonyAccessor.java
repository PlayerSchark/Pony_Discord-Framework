package io.schark.pony.feat.commands.annotation.impl;

import io.schark.pony.feat.commands.PonyChatCommandExecutor;

/**
 * @author Player_Schark
 */
public interface PonyAccessor {
	Long[] getIds(PonyChatCommandExecutor executor);
}
