package io.schark.pony.core;

import io.schark.pony.core.feat.PonyManagerListener;
import io.schark.pony.core.feat.audio.PonyManagerAudio;
import io.schark.pony.core.feat.commands.PonyManagerCommand;

/**
 * @author Player_Schark
 */
public record PonyManagerType<M extends PonyManager>(M manager) {
	public static final PonyManagerType<PonyManagerCommand> COMMAND = new PonyManagerType<>(new PonyManagerCommand());
	public static final PonyManagerType<PonyManagerListener> LISTENER = new PonyManagerType<>(new PonyManagerListener());
	public static final PonyManagerType<PonyManagerAudio> AUDIO = new PonyManagerType<>(new PonyManagerAudio());
}
