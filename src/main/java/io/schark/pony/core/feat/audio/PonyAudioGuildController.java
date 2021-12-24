package io.schark.pony.core.feat.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import io.schark.pony.core.PonyManagerType;
import io.schark.pony.core.feat.audio.handler.PonyAudioSendPlayerHandler;
import io.schark.pony.exception.JoinVoiceFailedException;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.function.Consumer;

@Getter
public class PonyAudioGuildController {

    private final Guild guild;
    private final AudioPlayer player;
    private final AudioPlayerManager audioPlayerManager;
    private final AudioManager guildManager;
    private volatile VoiceChannel channel;
    private volatile long[] timeout = new long[0];


    public PonyAudioGuildController(Guild guild, AudioPlayerManager audioPlayerManager) {
        this.guild = guild;
        this.audioPlayerManager = audioPlayerManager;
        this.player = audioPlayerManager.createPlayer();
        this.guildManager = this.guild.getAudioManager();
        this.guildManager.setSendingHandler(new PonyAudioSendPlayerHandler(player));
    }

    public static PonyAudioGuildController create(Guild guild) {
        return PonyManagerType.AUDIO.getManager().createGuildAudio(guild);
    }

    public void joinVoice(Member member, Consumer<PonySearchQuarry> consumer) {
        this.joinVoice(member, consumer, 0);
    }

    public void joinVoice(Member member, Consumer<PonySearchQuarry> consumer, long timeout) {
        GuildVoiceState state = member.getVoiceState();
        if (state == null) {
            throw new JoinVoiceFailedException("Bot can´t Connected whit a Voice Channel because the VoiceState is null");
        }
        this.joinVoice(state.getChannel(), consumer, timeout);
    }

    public void joinVoice(VoiceChannel channel,Consumer<PonySearchQuarry> consumer) {
        this.joinVoice(channel, consumer, 0);
    }

    public void joinVoice(VoiceChannel channel, Consumer<PonySearchQuarry> consumer, long timeout) {
        if (channel == null) {
            throw new JoinVoiceFailedException("Bot can´t Connected whit a Voice Channel because the Channel is null");
        }
        this.guildManager.openAudioConnection(channel);
        this.player.setVolume(10);
        this.channel = channel;
        this.timeout = new long[]{timeout};
        PonySearchQuarry quarry = new PonySearchQuarry(this.audioPlayerManager);
        consumer.accept(quarry);
    }

    public void voiceTimeout() {
        new Thread(() -> {
            for (;this.timeout[0] > 0; this.timeout[0]--) {
                 if (this.channel.getMembers().size() > 1) {
                     return;
                 }
            }
            this.guildManager.closeAudioConnection();
        }).start();
    }


}
