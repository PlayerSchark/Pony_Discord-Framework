package io.schark.pony.core.feat.audio;

import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.player.event.TrackStuckEvent;
import io.schark.pony.core.feat.audio.handler.PonyQueueHandler;
import lombok.AllArgsConstructor;

/**
 * @author Player_Slimey
 */
@AllArgsConstructor
public class PonyAudioListener implements AudioEventListener {
    private PonyQueueHandler queue;
    private PonyAudioGuildController controller;

    @Override
    public void onEvent(AudioEvent audioEvent) {
        System.out.println("Event called: " + audioEvent.getClass().getSimpleName());
        switch (audioEvent) {
            case TrackEndEvent event -> {
                System.out.println("Track ended");
                if (this.controller.getChannel() != null) {
                    if (this.queue.hasNext()) {
                        this.queue.nextTrack();
                    }
                    else {
                        System.out.println("No other Track found. Leaving voice");
                        boolean left = this.controller.leaveVoice(); //mutation here
                        assert left;
                    }
                }
            }
            case TrackStuckEvent event -> {
                System.out.println(event.thresholdMs);
                System.out.println("Stacktrace from event");
                StackTraceElement[] stackTrace = event.stackTrace;
                for (StackTraceElement stackTraceElement : stackTrace) {
                    System.out.println(stackTraceElement.toString());
                }
                System.out.println();
                System.out.println("Stacktrace:");
                for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                    System.out.println(stackTraceElement.toString());
                }
            }
            default -> {}
        }
    }
}
