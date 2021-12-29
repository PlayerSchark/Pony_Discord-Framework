package io.schark.pony.core.feat.audio.handler;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Player_Schark
 */
@Getter
public class PonyQueueHandler {
	private final AudioPlayer player;
	private final List<AudioTrack> audioTracks;
	private AudioTrack nowTrack;
	private AudioTrack beforeTrack;
	private int trackSize = 0;
	private boolean loop;
	private boolean queueLoop;

	public PonyQueueHandler(AudioPlayer player, List<AudioTrack> audioTracks) {
		this.player = player;
		this.audioTracks = audioTracks;
	}

	public void playFirstTrack() {
		AudioTrack track = this.getAudioTracks().get(0);
		player.playTrack(track);
		this.setNowTrack(track);
	}

	public void nextTrack() {
		AudioTrack nextTrack =
				this.getNowTrack() == null ? this.getAudioTracks().get(0)
						: this.isLoop() ? this.getNowTrack()
						: this.getAudioTracks().get(trackSize);
		this.playTrack(nextTrack);
	}

	public void nextTrackAndBreakAllLoop() {
		AudioTrack nextTrack = this.getNowTrack() == null ? this.getAudioTracks().get(0)
				: this.getAudioTracks().get(trackSize);
		this.loopCurrentTrack(false);
		this.loopQueue(false);
		this.playTrack(nextTrack);
	}

	public void nextTrackAndBreakTrackLoop() {
		AudioTrack nextTrack = this.getNowTrack() == null ? this.getAudioTracks().get(0)
				: this.getAudioTracks().get(trackSize);
		this.loopCurrentTrack(false);
		this.playTrack(nextTrack);
	}

	public void nextTrackAndBreakQueueLoop() {
		AudioTrack nextTrack = this.getNowTrack() == null ? this.getAudioTracks().get(0)
				: this.getAudioTracks().get(trackSize);
		this.loopQueue(false);
		this.playTrack(nextTrack);
	}

	public void loopCurrentTrack(boolean loop) {
		this.loop = loop;
	}

	public void loopQueue(boolean loop) {
		this.queueLoop = loop;
	}

	public void setNowTrack(AudioTrack track) {
		this.nowTrack = track;
	}

	public void setBeforeTrack(AudioTrack beforeTrack) {
		this.beforeTrack = beforeTrack;
	}

	public void setCurrentTrackSize(int size) {
		this.trackSize = size;
	}

	public void addTrackSize(int size) {
		this.trackSize = trackSize + size;
	}

	private void playTrack(AudioTrack track) {
		this.setBeforeTrack(this.getNowTrack());
		this.setNowTrack(track);
		this.player.playTrack(track);
		if (this.isQueueLoop()) {
			if (this.getAudioTracks().size() <= this.getTrackSize()) {
				this.setCurrentTrackSize(0);
			} else {
				this.addTrackSize(1);
			}
		} else if (!this.isLoop()) {
			this.addTrackSize(1);
		}
	}
}
