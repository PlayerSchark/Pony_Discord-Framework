package io.schark.pny.example;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.schark.pony.core.feat.audio.handler.PonyPlaylistHandler;
import io.schark.pony.core.feat.commands.command.PonyCommand;

/**
 * @author Player_Schark
 */
public class PlayListHandler extends PonyPlaylistHandler {

	@Override protected void beforeTrack(PonyCommand command, AudioTrack audioTrack) {

	}

	@Override protected void afterTrack(PonyCommand command, AudioTrack audioTrack) {

	}

	@Override protected void before(PonyCommand command, AudioPlaylist playlist) {

	}

	@Override protected void after(PonyCommand command, AudioPlaylist playlist) {

	}
}
