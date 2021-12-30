package io.schark.pony.core.feat.audio;

import com.sedmelluq.discord.lavaplayer.player.event.*;
import io.schark.pony.core.feat.audio.handler.PonyQueueHandler;
import lombok.AllArgsConstructor;

import java.util.Arrays;

/**
 * @author Player_Slimey
 */
@AllArgsConstructor
public class PonyAudioListener implements AudioEventListener {
    private PonyQueueHandler handler;
    private PonyAudioGuildController controller;

    @Override
    public void onEvent(AudioEvent audioEvent) {
        System.out.println("Event called: " + audioEvent.getClass().getSimpleName());
        switch (audioEvent) {
            case TrackEndEvent event -> {
                System.out.println("endEvent");
                if (controller.getChannel() != null) {
                    this.handler.nextTrack();
                }
            }
            case TrackStuckEvent event -> {
                System.out.println(event.thresholdMs);
                StackTraceElement[] stackTrace = event.stackTrace;
                System.out.println(Arrays.toString(stackTrace));
            }
            default -> {}
        }
    }
}
