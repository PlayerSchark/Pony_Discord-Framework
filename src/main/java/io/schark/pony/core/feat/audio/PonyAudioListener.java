package io.schark.pony.core.feat.audio;

import com.sedmelluq.discord.lavaplayer.player.event.*;
import io.schark.pony.core.feat.audio.handler.PonyQueueHandler;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PonyAudioListener implements AudioEventListener {
    private PonyQueueHandler handler;

    @Override
    public void onEvent(AudioEvent audioEvent) {
        if (audioEvent instanceof TrackEndEvent) {
            this.handler.nextTrack();
        }
    }
}
