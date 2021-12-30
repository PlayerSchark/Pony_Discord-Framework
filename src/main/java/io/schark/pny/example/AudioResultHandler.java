package io.schark.pny.example;

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.schark.pony.core.feat.audio.PonyAudioGuildController;
import io.schark.pony.core.feat.audio.handler.PonyAudioResultHandler;
import io.schark.pony.core.feat.audio.handler.PonyQueueHandler;
import io.schark.pony.core.feat.commands.command.PonyChatCommand;

public class AudioResultHandler extends PonyAudioResultHandler<PonyChatCommand> {

    public AudioResultHandler(PonyAudioGuildController controller, PonyChatCommand command) {
        super(controller, command);
    }

    @Override public void trackLoaded(PonyChatCommand command, AudioTrack audioTrack) {
        this.playTrack(audioTrack);
        //command.answer("playing track");
    }

    @Override public void queueTrackLoaded(PonyChatCommand command, AudioTrack audioTrack) {

    }

    @Override
    public void playlistLoaded(PonyChatCommand command, AudioPlaylist audioPlaylist) {
        if (this.getController().getQueue() == null) {
            PonyQueueHandler handler = new PonyQueueHandler(this.getAudioPlayer(), this.getController());
            handler.addNewPlayList(audioPlaylist.getTracks());
            this.getController().setQueue(handler);
            handler.nextTrack();
        }

        command.answer("playing playlist");
    }

    @Override public void noMatches(PonyChatCommand command) {
        command.answer("No matches");
    }

    @Override public void loadFailed(PonyChatCommand command, FriendlyException e) {
        command.answer("loading failed");
    }

    @Override protected void queueLoadFailed(PonyChatCommand command, FriendlyException e) {

    }
}
