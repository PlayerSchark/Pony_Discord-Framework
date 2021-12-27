package io.schark.pony.core.feat.audio;

import com.google.common.base.Joiner;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import io.schark.pony.Pony;
import io.schark.pony.core.PonyManagerType;
import io.schark.pony.core.feat.audio.handler.PonyAudioResultHandler;
import io.schark.pony.core.feat.commands.in.PonyArg;
import io.schark.pony.exception.MusicSearchException;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class PonySearchQuerry {
	private final AudioPlayerManager audioPlayerManager;
	private static SpotifyApi spotifyApi;

	private static void protocolCheck(String url) {
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			throw new MusicSearchException("The querry must start with 'http(s)://'");
		}
	}

	private static boolean regexMatch(Function<PonyManagerAudio, Pattern> regex, String url) {
		PonyManagerAudio manager = PonyManagerType.AUDIO.manager();
		return regex.apply(manager).matcher(url).find();
	}

	private static void regexCheck(Function<PonyManagerAudio, Pattern> regex, String url) {
		if (!PonySearchQuerry.regexMatch(regex, url)) {
			throw new MusicSearchException("The querry is no valid url");
		}
	}

	public void searchYoutubeUrl(String url, PonyAudioResultHandler handler) {
		PonySearchQuerry.protocolCheck(url);
		PonySearchQuerry.regexCheck(PonyManagerAudio::getYoutubeRegex, url);
		this.searchYoutube(url, handler);
	}

	public void searchYoutube(String querry, PonyAudioResultHandler handler) {
		String uri = "ytsearch:" + querry;
		System.out.println("Loading youtube uri " + uri);
		this.audioPlayerManager.loadItem(uri, handler);
		System.out.println("Youtube uri " + uri + " loaded");
	}

	public void searchYoutube(List<PonyArg<String>> args, PonyAudioResultHandler handler) {
		String querry = null;
		for (PonyArg<String> arg : args) {
			Matcher matcher = PonyManagerType.AUDIO.manager().getYoutubeRegex().matcher(arg.getContent());
			if (matcher.find()) {
				querry = matcher.group();
				break;
			}
		}

		querry = querry != null ? querry : this.joinArgs(args);
		this.searchYoutube(querry, handler);
	}

	private String joinArgs(List<PonyArg<String>> args) {
		List<String> strArgs = new ArrayList<>();

		for (PonyArg<String> arg : args) {
			strArgs.add(arg.getContent());
		}

		return Joiner.on(' ').join(strArgs);
	}

	/**
	 * searches a spotify track and uses youtube to load songs
	 *
	 * @param uri     can be trackId spotify uri or url
	 * @param handler result handler
	 */
	public void searchSpotify(String uri, PonyAudioResultHandler handler) {
		SpotifyApi spotifyApi = this.getSpotifyApi();
		Track track;
		try {
			track = spotifyApi.getTrack(uri).build().execute();
		}
		catch (IOException | SpotifyWebApiException | ParseException e) {
			e.printStackTrace();
			throw new MusicSearchException("Could not send request to spotify.");
		}
		this.searchYoutubeUrl(track.getName(), handler);
	}

	private SpotifyApi getSpotifyApi() {
		return spotifyApi == null ? this.buildSpotifyApi() : spotifyApi;
	}

	private SpotifyApi buildSpotifyApi() {
		String spotifyAccessToken;
		String refreshToken;
		try {
			refreshToken = spotifyAccessToken = Pony.getInstance().getConfig().getSpotifyAccessToken();
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new MusicSearchException("Invalid access token.");
		}
		return new SpotifyApi.Builder().setAccessToken(spotifyAccessToken).setRefreshToken(refreshToken).build();
	}
}
