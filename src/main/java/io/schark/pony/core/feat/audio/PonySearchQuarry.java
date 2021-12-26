package io.schark.pony.core.feat.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import io.schark.pony.core.PonyManagerType;
import io.schark.pony.exception.MusicSearchURLException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PonySearchQuarry {
	private final AudioPlayerManager audioPlayerManager;

	public void searchYoutube(String content, PonyAudioResult result) {
		if (!content.startsWith("http://") && !content.startsWith("https://")) {
			throw new MusicSearchURLException("The Argument start no with 'http(s)://'");
		}
		if (!PonyManagerType.AUDIO.manager().getYoutubeRegex().matcher(content).find()) {
			throw new MusicSearchURLException("The Argument is no Youtube URL");
		}
		String url = "ytsearch:" + content;
		System.out.println("before loadItem");
		this.audioPlayerManager.loadItem(url, result);
		System.out.println("after loadItem");
	}
}
