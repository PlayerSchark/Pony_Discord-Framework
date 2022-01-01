package io.schark.pony.core.feat.audio.handler;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import io.schark.pony.core.feat.audio.PonyAudioGuildController;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.Deque;
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
	@Setter private boolean loop;
	@Setter private boolean queueLoop;

	public PonyQueueHandler(AudioPlayer player, PonyAudioGuildController controller) {
		this.player = player;
		this.audioTracks = new LinkedList<>();
		this.controller = controller;
	}

	private Deque<AudioTrackInfo> audioInfo(List<AudioTrack> audioTracks) {
		Deque<AudioTrackInfo> list = new LinkedList<>();
		for (AudioTrack track : audioTracks) {
			list.add(track.getInfo());
		}
		return list;
	}

	public void addNewPlayList(List<AudioTrack> audioTracks) {
		this.audioTracks.addAll(this.audioInfo(audioTracks));
	}

	public void addTrackInfo(AudioTrackInfo track) {
		this.audioTracks.add(track);
	}

	public synchronized void nextTrack() {
		if (!this.isLoop() && this.isQueueLoop()) {
			this.audioTracks.add(this.currentInfo);
		}
		boolean loop = this.isLoop();
		AudioTrackInfo nextTrack = loop
						? this.getCurrentInfo()
						: this.getAudioTracks().poll();
		String asString = nextTrack == null ? "null" : (nextTrack.title + ":" + nextTrack.identifier);
		System.out.println("load next: " + asString);
		this.loadTrack(nextTrack);
	}

	public synchronized void nextTrackAndBreakAllLoop() {
		AudioTrackInfo nextTrack = this.getAudioTracks().pop();
		this.setLoop(false);
		this.setQueueLoop(false);
		this.loadTrack(nextTrack);
	}

	public synchronized void nextTrackAndBreakTrackLoop() {
		AudioTrackInfo nextTrack = this.getAudioTracks().pop();
		this.setLoop(false);
		this.loadTrack(nextTrack);
	}

	public synchronized void nextTrackAndBreakQueueLoop() {
		AudioTrackInfo nextTrack = this.getAudioTracks().pop();
		this.setQueueLoop(false);
		this.loadTrack(nextTrack);
	}

	public synchronized void reset() {
		this.loop = false;
		this.queueLoop = false;
		this.clear();
	}

	public synchronized void clear() {
		this.setCurrentInfo(null);
		this.audioTracks.clear();
	}

	public void shuffle() {
		Collections.shuffle((List<?>) this.audioTracks);
	}

	public synchronized void loadTrack(AudioTrackInfo info) {
		this.setCurrentInfo(info);
		this.loadInfoIntoController();
	}

	private synchronized void loadInfoIntoController() {
		AudioTrackInfo info = this.getCurrentInfo();
		this.controller.getAudioPlayerManager().loadItem(info.identifier, this.controller.getQueueResultHandler());
	}

	public boolean hasNext() {
		return this.isLoop() || this.isQueueLoop() && this.currentInfo != null || this.audioTracks.iterator().hasNext();
	}
}
