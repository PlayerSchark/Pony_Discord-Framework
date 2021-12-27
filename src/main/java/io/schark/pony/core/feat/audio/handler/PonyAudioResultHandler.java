package io.schark.pony.core.feat.audio.handler;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.schark.pny.example.PlayListHandler;
import io.schark.pony.core.feat.audio.PonyAudioGuildController;
import io.schark.pony.core.feat.commands.command.PonyCommand;
import lombok.Getter;

import java.util.List;

@Getter
public abstract class PonyAudioResultHandler<C extends PonyCommand> implements AudioLoadResultHandler {

	private final AudioPlayer audioPlayer;
	private final C command;

	public PonyAudioResultHandler(PonyAudioGuildController controller, C command) {
		this.audioPlayer = controller.getAudioPlayer();
		this.command = command;
	}

	protected void playTrack(AudioTrack audioTrack) {
		this.getAudioPlayer().playTrack(audioTrack);
	}

	protected void playTrack(List<AudioTrack> tracks, PlayListHandler playListHandler) {
	}

	@Override public void trackLoaded(AudioTrack audioTrack) {
		this.trackLoaded(this.command, audioTrack);
	}

	public abstract void trackLoaded(C command, AudioTrack audioTrack);

	@Override public void playlistLoaded(AudioPlaylist audioPlaylist) {
		this.playlistLoaded(this.command, audioPlaylist);
	}

	public abstract void playlistLoaded(C command, AudioPlaylist audioPlaylist);

	@Override public void noMatches() {
		this.noMatches(this.command);
	}

	public abstract void noMatches(C command);

	@Override public void loadFailed(FriendlyException e) {
		this.loadFailed(this.command, e);
	}

	public abstract void loadFailed(C command, FriendlyException e);
}
