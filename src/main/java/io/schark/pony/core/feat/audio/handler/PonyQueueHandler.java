package io.schark.pony.core.feat.audio.handler;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import io.schark.pony.core.feat.audio.PonyAudioGuildController;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Player_Slimey
 */
@Getter
public class PonyQueueHandler {
	private final AudioPlayer player;
	private final PonyAudioGuildController controller;
	private Deque<AudioTrackInfo> audioTracks;
	@Setter private AudioTrackInfo currentInfo;
	@Setter private AudioTrack currentTrack;
	@Setter private AudioTrack beforeTrack;
	@Setter private boolean loop;
	@Setter private boolean queueLoop;

	public PonyQueueHandler(AudioPlayer player, List<AudioTrack> audioTracks, PonyAudioGuildController controller) {
		this.player = player;
		this.audioTracks = this.audioInfo(audioTracks);
		this.controller = controller;
	}

	private Deque<AudioTrackInfo> audioInfo(List<AudioTrack> audioTracks) {
		Deque<AudioTrackInfo> list = new LinkedList<>();
		for (AudioTrack track : audioTracks) {
			list.add(track.getInfo());
		}
		return list;
	}

	private AudioTrack getFromCurrentInfo() {
		AudioTrackInfo info = this.getCurrentInfo();
		this.controller.loadItem(info);
		return this.getCurrentTrack();
	}

	public void addNewPlayList(List<AudioTrack> audioTracks) {
		this.audioTracks = this.audioInfo(audioTracks);
	}

	public void addTrackInfo(AudioTrackInfo track) {
		this.audioTracks.add(track);
	}

	public void nextTrack() {
		AudioTrackInfo nextTrack = this.isLoop()
				? this.getCurrentInfo()
				: this.isQueueLoop()
				? this.getAudioTracks().poll() : this.getAudioTracks().pop();
		System.out.println(nextTrack);
		this.playTrack(nextTrack);
	}

	public void nextTrackAndBreakAllLoop() {
		AudioTrackInfo nextTrack = this.getAudioTracks().pop();
		this.setLoop(false);
		this.setQueueLoop(false);
		this.playTrack(nextTrack);
	}

	public void nextTrackAndBreakTrackLoop() {
		AudioTrackInfo nextTrack = this.getAudioTracks().pop();
		this.setLoop(false);
		this.playTrack(nextTrack);
	}

	public void nextTrackAndBreakQueueLoop() {
		AudioTrackInfo nextTrack = this.getAudioTracks().pop();
		this.setQueueLoop(false);
		this.playTrack(nextTrack);
	}

	private void playTrack(AudioTrackInfo info) {
		this.setBeforeTrack(this.getCurrentTrack());
		this.setCurrentInfo(info);
		AudioTrack track = this.getFromCurrentInfo();
		this.setCurrentTrack(track);
	}
}
