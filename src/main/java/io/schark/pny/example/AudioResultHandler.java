package io.schark.pny.example;

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.schark.pony.core.feat.audio.PonyAudioResult;

public class AudioResultHandler extends PonyAudioResultHandler<PonyChatCommand> {

    public AudioResultHandler(PonyAudioGuildController controller, PonyChatCommand command) {
        super(controller, command);
    }

    @Override public void trackLoaded(PonyChatCommand command, AudioTrack audioTrack) {
        this.playTrack(audioTrack);
        command.answer("playing track");
    }

    @Override public void playlistLoaded(PonyChatCommand command, AudioPlaylist audioPlaylist) {
        this.playTrack(audioPlaylist.getTracks(), new PlayListHandler());
        command.answer("playing playlist track 1");
    }

    @Override public void noMatches(PonyChatCommand command) {
        command.answer("No matches");
    }

    @Override public void loadFailed(PonyChatCommand command, FriendlyException e) {
        command.answer("loading failed");
    }
}
