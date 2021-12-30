package io.schark.pony.core.feat.audio.handler;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

/**
 * @author Player_Slimey
 */
@RequiredArgsConstructor
public class PonyAudioSendPlayerHandler implements AudioSendHandler {

    private final AudioPlayer audioPlayer;
    private AudioFrame lastFrame;

    @Override
    public boolean canProvide() {
        this.lastFrame = this.audioPlayer.provide();
        return this.lastFrame != null;
    }

    @Nullable
    @Override
    public ByteBuffer provide20MsAudio() {
        return ByteBuffer.wrap(this.lastFrame.getData());
    }

    public boolean isOpus() {
        return true;
    }
}
