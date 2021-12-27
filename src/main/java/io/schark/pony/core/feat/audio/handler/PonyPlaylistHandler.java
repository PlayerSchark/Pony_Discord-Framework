package io.schark.pony.core.feat.audio.handler;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.schark.pony.core.feat.commands.command.PonyCommand;

/**
 * @author Player_Schark
 */
public abstract class PonyPlaylistHandler<C extends PonyCommand> {

	protected void next() {
	}

	protected abstract void beforeTrack(C command, AudioTrack audioTrack);

	protected abstract void afterTrack(C command, AudioTrack audioTrack);

	protected abstract void before(C command, AudioPlaylist playlist);

	protected abstract void after(C command, AudioPlaylist playlist);

}
