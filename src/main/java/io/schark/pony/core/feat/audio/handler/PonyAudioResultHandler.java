package io.schark.pony.core.feat.audio.handler;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.schark.pony.core.feat.audio.PonyAudioGuildController;
import io.schark.pony.core.feat.commands.command.PonyCommand;
import lombok.Getter;

/**
 * @author Player_Slimey
 */
@Getter
public abstract class PonyAudioResultHandler<C extends PonyCommand<?, ?>> implements AudioLoadResultHandler {

	private final AudioPlayer audioPlayer;
	private final C command;
	private final PonyAudioGuildController controller;

	public PonyAudioResultHandler(PonyAudioGuildController controller, C command) {
		this.controller = controller;
		this.audioPlayer = this.controller.getAudioPlayer();
		this.command = command;
	}

	protected void playTrack(AudioTrack audioTrack) {
		System.out.println("playing track " + audioTrack.getInfo().title);
		this.getAudioPlayer().playTrack(audioTrack);
	}

	@Override public void trackLoaded(AudioTrack audioTrack) {
		System.out.println("loaded track " + audioTrack.getInfo().title);
		this.trackLoaded(this.command, audioTrack);
	}

	public abstract void trackLoaded(C command, AudioTrack audioTrack);

	protected void queueTrackLoaded(AudioTrack audioTrack) {
		System.out.println("loaded track from playlist " + audioTrack.getInfo().title);
		this.queueTrackLoaded(this.command, audioTrack);
	}

	public abstract void queueTrackLoaded(C command, AudioTrack audioTrack);

	@Override public void playlistLoaded(AudioPlaylist audioPlaylist) {
		System.out.println("loaded playlist " + audioPlaylist.getName());
		this.playlistLoaded(this.command, audioPlaylist);
	}

	public abstract void playlistLoaded(C command, AudioPlaylist audioPlaylist);

	@Override public void noMatches() {
		System.out.println("no matches for " + this.command.getRawArgContent());
		this.noMatches(this.command);
	}

	public abstract void noMatches(C command);

	@Override public void loadFailed(FriendlyException e) {
		this.loadFailed(this.command, e);
	}

	public abstract void loadFailed(C command, FriendlyException e);

	public void queueLoadFailed(FriendlyException e) {
		this.queueLoadFailed(this.command, e);
	}

	protected abstract void queueLoadFailed(C command, FriendlyException e);
}
