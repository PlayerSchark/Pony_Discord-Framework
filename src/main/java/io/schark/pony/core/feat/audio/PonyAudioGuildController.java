package io.schark.pony.core.feat.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.schark.pony.core.PonyManagerType;
import io.schark.pony.core.feat.audio.handler.PonyAudioSendPlayerHandler;
import io.schark.pony.core.feat.commands.command.IPonyGuildable;
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
        this.guildManager.setSendingHandler(new PonyAudioSendPlayerHandler(this.player));
    }

    public static PonyAudioGuildController create(IPonyGuildable guildable) {
        Guild guild = guildable.getGuild();
        return PonyManagerType.AUDIO.manager().createGuildAudio(guild);
    }

    public static PonyAudioGuildController create(Guild guild) {
        return PonyManagerType.AUDIO.manager().createGuildAudio(guild);
    }

    /**
     * joins a channel and instantly leaves if the channel is empty.
     *
     * @param member command member to join
     * @param onJoin action that sould be executed on join
     */
    public void joinVoice(Member member, Consumer<PonySearchQuarry> onJoin) {
        this.joinVoice(member, onJoin, 0);
    }

    /**
     * joins a channel and automaticly leaves if the channel is empty.
     * -1 == no autoleave;
     * 0 == instant leave
     * i > 0 leave after i milli seconds
     *
     * @param member  command member to join
     * @param onJoin  action that sould be executed on join
     * @param timeout timeout to leave the channel if the channel is empty.
     */
    public void joinVoice(Member member, Consumer<PonySearchQuarry> onJoin, long timeout) {
        GuildVoiceState state = member.getVoiceState();
        if (state == null) {
            throw new JoinVoiceFailedException("Bot can´t Connected whit a Voice Channel because the VoiceState is null");
        }
        this.joinVoice(state.getChannel(), onJoin, timeout);
    }

    /**
     * joins a channel and instantly leaves if the channel is empty.
     *
     * @param command command to extract member from
     * @param onJoin  action that sould be executed on join
     */
    public void joinVoice(IPonyGuildable command, Consumer<PonySearchQuarry> onJoin) {
        this.joinVoice(command, onJoin, 0);
    }

    /**
     * joins a channel and automaticly leaves if the channel is empty.
     * -1 == no autoleave;
     * 0 == instant leave
     * i > 0 leave after i milli seconds
     *
     * @param command command to extract member from
     * @param onJoin  action that sould be executed on join
     * @param timeout timeout to leave the channel if the channel is empty.
     */
    public void joinVoice(IPonyGuildable command, Consumer<PonySearchQuarry> onJoin, long timeout) {
        Member member = command.getFirstMentionedOrSender();
        GuildVoiceState state = member.getVoiceState();
        if (state == null) {
            return;
        }
        this.joinVoice(state.getChannel(), onJoin, timeout);
    }

    /**
     * joins a channel and instantly leaves if the channel is empty.
     *
     * @param channel channel to join
     * @param onJoin  action that sould be executed on join
     */
    public void joinVoice(VoiceChannel channel, Consumer<PonySearchQuarry> onJoin) {
        this.joinVoice(channel, onJoin, 0);
    }

    /**
     * joins a channel and automaticly leaves if the channel is empty.
     * -1 == no autoleave;
     * 0 == instant leave
     * i > 0 leave after i milli seconds
     *
     * @param channel channel to join
     * @param onJoin  action that sould be executed on join
     * @param timeout timeout to leave the channel if the channel is empty.
     */
    public void joinVoice(VoiceChannel channel, Consumer<PonySearchQuarry> onJoin, long timeout) {
        if (channel == null) {
            throw new JoinVoiceFailedException("Bot can´t Connected whit a Voice Channel because the Channel is null");
        }
        this.guildManager.openAudioConnection(channel);
        this.player.setVolume(10);
        this.channel = channel;
        this.timeout = new long[] { timeout, timeout };
        PonySearchQuarry quarry = new PonySearchQuarry(this.audioPlayerManager);
        onJoin.accept(quarry);
    }

    public void moveBotToVoice(VoiceChannel channel) {
        this.moveBotToVoice(channel, null);
    }

    public void moveBotToVoice(VoiceChannel channel, Consumer<PonySearchQuarry> onJoin) {
        if (channel == null) {
            throw new JoinVoiceFailedException("Bot can´t Connected whit a Voice Channel because the Channel is null");
        }
        this.guildManager.openAudioConnection(channel);
        this.channel = channel;
        this.timeout[0] = this.timeout[1];
        if (onJoin != null) {
            PonySearchQuarry quarry = new PonySearchQuarry(this.audioPlayerManager);
            onJoin.accept(quarry);
        }
    }



    public boolean leaveVoice() {
        if (this.channel == null) {
            return false;
        }
        this.guildManager.closeAudioConnection();
        return true;
    }

    public void setSpeakVolume(int volume) {
        this.player.setVolume(volume);
    }

    public void setAutoReconnected(boolean reconnected) {
        this.getGuildManager().setAutoReconnect(reconnected);
    }

    public void setNewTimeout(int timeout) {
        this.timeout = new long[]{timeout, timeout};
    }

    public void resetTimeout() {
        this.timeout[0] = this.timeout[1];
    }

    public void stopTrack() {
        this.player.stopTrack();
    }

    public boolean pause() {
        if (this.isPaused()) {
            return false;
        }
        this.player.setPaused(true);
        return true;
    }

    public boolean play() {
        if (!this.isPaused()) {
            return false;
        }
        this.player.setPaused(false);
        return true;
    }

    public boolean isPaused() {
        return this.player.isPaused();
    }

    public AudioTrack getPlayingTrack() {
        return this.player.getPlayingTrack();
    }

    public void voiceTimeout() {
        if (this.timeout[0] == -1) {
            return;
        }
        new Thread(() -> {
            for (;this.timeout[0] > 0; this.timeout[0]--) {
                 if (this.channel.getMembers().size() > 1) {
                     this.timeout[0] = this.timeout[1];
                     return;
                 }
            }
            this.guildManager.closeAudioConnection();
            this.channel = null;
            this.timeout = new long[0];
        }).start();
    }


}
