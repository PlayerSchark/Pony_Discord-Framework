package io.schark.pony.core;

import io.schark.pony.core.feat.PonyManagerListener;
import io.schark.pony.core.feat.audio.PonyManagerAudio;
import io.schark.pony.core.feat.commands.PonyManagerCommand;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Player_Schark
 */
@Getter
@RequiredArgsConstructor
public class PonyManagerType<M extends PonyManager> {
	public static final PonyManagerType<PonyManagerCommand> COMMAND = new PonyManagerType<>(new PonyManagerCommand());
	public static final PonyManagerType<PonyManagerListener> LISTENER = new PonyManagerType<>(new PonyManagerListener());
	public static final PonyManagerType<PonyManagerAudio> AUDIO = new PonyManagerType<>(new PonyManagerAudio());

	private final M manager;
}
