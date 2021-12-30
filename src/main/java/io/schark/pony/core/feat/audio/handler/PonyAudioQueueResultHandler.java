package io.schark.pony.core.feat.audio.handler;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.RequiredArgsConstructor;

/**
 * @author Player_Schark
 */
@RequiredArgsConstructor
public final class PonyAudioQueueResultHandler implements AudioLoadResultHandler {
	private final AudioPlayer audioPlayer;
	private final PonyAudioResultHandler resultHandler;

	@Override public void trackLoaded(AudioTrack audioTrack) {
		this.resultHandler.queueTrackLoaded(audioTrack);
	}

	@Override public void playlistLoaded(AudioPlaylist audioPlaylist) {
		throw new IllegalStateException("Unsupported method call playlistLoaded");
	}

	@Override public void noMatches() {
		throw new IllegalStateException("Unexpected method call. Information sould be always present");
	}

	@Override public void loadFailed(FriendlyException e) {
		this.resultHandler.queueLoadFailed(e);
	}
}
