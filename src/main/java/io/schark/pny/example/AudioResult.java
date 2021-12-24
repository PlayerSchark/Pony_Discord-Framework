package io.schark.pny.example;

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.schark.pony.core.feat.audio.PonyAudioResult;

public class AudioResult extends PonyAudioResult {

    @Override
    public void trackLoaded(AudioTrack audioTrack) {

    }

    @Override
    public void playlistLoaded(AudioPlaylist audioPlaylist) {

    }

    @Override
    public void noMatches() {

    }

    @Override
    public void loadFailed(FriendlyException e) {

    }
}
