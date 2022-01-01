package io.schark.pony.core.feat.audio;

import com.google.common.base.Joiner;
import io.schark.pony.Pony;
import io.schark.pony.core.PonyManagerType;
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

/**
 * @author Player_Schark
 */
@RequiredArgsConstructor
public class PonySearchQuery {
	private final PonyAudioGuildController controller;
	private static SpotifyApi spotifyApi;

	private static void protocolCheck(String url) {
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			throw new MusicSearchException("The query must start with 'http(s)://'");
		}
	}

	private static boolean regexMatch(Function<PonyManagerAudio, Pattern> regex, String url) {
		PonyManagerAudio manager = PonyManagerType.AUDIO.manager();
		return regex.apply(manager).matcher(url).find();
	}

	private static void regexCheck(Function<PonyManagerAudio, Pattern> regex, String url) {
		if (!PonySearchQuery.regexMatch(regex, url)) {
			throw new MusicSearchException("The query is no valid url");
		}
	}

	public void searchYoutubeUrl(String url) {
		PonySearchQuery.protocolCheck(url);
		PonySearchQuery.regexCheck(PonyManagerAudio::getYoutubeRegex, url);
		this.searchYoutube(url);
	}

	public void searchYoutube(String query) {
		boolean isUrl = PonySearchQuery.regexMatch(PonyManagerAudio::getYoutubeRegex, query);
		String prefix = isUrl ? "" : "ytsearch:";
		String uri = prefix + query;
		System.out.println("Loading youtube uri " + uri);
		this.controller.loadItem(uri, isUrl);
		System.out.println("Youtube uri " + uri + " loaded");
	}

	public void searchYoutube(List<PonyArg<String>> args) {
		String query = null;
		for (PonyArg<String> arg : args) {
			Matcher matcher = PonyManagerType.AUDIO.manager().getYoutubeRegex().matcher(arg.getContent());
			if (matcher.find()) {
				query = matcher.group();
				break;
			}
		}

		query = query != null ? query : this.joinArgs(args);
		this.searchYoutube(query);
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
	 */
	public void searchSpotify(String uri) {
		SpotifyApi spotifyApi = this.getSpotifyApi();
		Track track;
		try {
			track = spotifyApi.getTrack(uri).build().execute();
		}
		catch (IOException | SpotifyWebApiException | ParseException e) {
			e.printStackTrace();
			throw new MusicSearchException("Could not send request to spotify.");
		}
		this.searchYoutubeUrl(track.getName());
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
