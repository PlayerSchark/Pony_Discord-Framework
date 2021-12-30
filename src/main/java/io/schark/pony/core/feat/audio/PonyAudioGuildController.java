package io.schark.pony.core.feat.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import io.schark.pony.core.PonyManagerType;
import io.schark.pony.core.feat.audio.handler.PonyAudioResultHandler;
import io.schark.pony.core.feat.audio.handler.PonyAudioSendPlayerHandler;
import io.schark.pony.core.feat.audio.handler.PonyQueueHandler;
import io.schark.pony.core.feat.commands.command.IPonyGuildable;
import io.schark.pony.core.feat.commands.command.PonyCommand;
import io.schark.pony.exception.JoinVoiceFailedException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Player_Slimey
 */
@Getter
public class PonyAudioGuildController {

    private static final int TIMEOUT_COUNTER = 0;
    private static final int DEFAULT_TIMEOUT = 1;
    private final Guild guild;
    private AudioPlayer audioPlayer;
    private final AudioPlayerManager audioPlayerManager;
    private final AudioManager guildManager;
    private PonyQueueHandler queueHandler;
    @Setter(AccessLevel.PRIVATE) private PonyAudioResultHandler<?> resultHandler;
    private volatile VoiceChannel channel;
    private volatile long[] timeouts = new long[0];

    protected PonyAudioGuildController(Guild guild, AudioPlayerManager audioPlayerManager) {
        this.guild = guild;
        this.audioPlayerManager = audioPlayerManager;
        this.audioPlayer = audioPlayerManager.createPlayer();
        this.guildManager = this.guild.getAudioManager();
        this.guildManager.setSendingHandler(new PonyAudioSendPlayerHandler(this.audioPlayer));
    }

    public static <C extends PonyCommand<?, ?>> PonyAudioGuildController create(IPonyGuildable guildable, Function<PonyAudioGuildController, PonyAudioResultHandler<C>> controller) {
        Guild guild = guildable.getGuild();
        return PonyAudioGuildController.create(guild, controller);
    }

    public static <C extends PonyCommand<?, ?>> PonyAudioGuildController create(Guild guild, Function<PonyAudioGuildController, PonyAudioResultHandler<C>> controller) {
        PonyAudioGuildController guildController = PonyManagerType.AUDIO.manager().createGuildAudio(guild);
        PonyAudioResultHandler<C> resultHandler = controller.apply(guildController);
        guildController.setResultHandler(resultHandler);
        return guildController;
    }

    public void loadQueueHandler(PonyQueueHandler handler) {
        this.queueHandler = handler;
        this.queueHandler.getPlayer().addListener(new PonyAudioListener(handler, this));
        this.audioPlayer = this.queueHandler.getPlayer();
    }

    public void loadAudioResultHandler(PonyAudioResultHandler handler) {
        this.resultHandler = handler;
    }

    /**
     * joins a channel and instantly leaves if the channel is empty.
     *
     * @param member command member to join
     * @param onJoin action that sould be executed on join
     */
    public void joinVoice(Member member, Consumer<PonySearchQuerry> onJoin) {
        this.joinVoice(member, onJoin, 0);
    }

    /**
     * joins a channel and automatically leaves if the channel is empty.
     * -1 == no auto leave;
     * 0 == instant leave
     * i > 0 leave after i millis
     *
     * @param member  command member to join
     * @param onJoin  action that should be executed on join
     * @param timeout timeout to leave the channel if the channel is empty.
     */
    public void joinVoice(Member member, Consumer<PonySearchQuerry> onJoin, long timeout) {
        GuildVoiceState state = member.getVoiceState();
        if (state == null) {
            throw new JoinVoiceFailedException("Bot can't Connected whit a Voice Channel because the VoiceState is null");
        }
        this.joinVoice(state.getChannel(), onJoin, timeout);
    }

    /**
     * joins a channel and instantly leaves if the channel is empty.
     *
     * @param command command to extract member from
     * @param onJoin  action that should be executed on join
     */
    public void joinVoice(IPonyGuildable command, Consumer<PonySearchQuerry> onJoin, String failMessage) {
        this.joinVoice(command, onJoin, 0, failMessage);
    }

    /**
     * joins a channel and automatically leaves if the channel is empty.
     * -1 == no auto leave;
     * 0 == instant leave
     * i > 0 leave after i millis
     *
     * @param command command to extract member from
     * @param onJoin  action that should be executed on join
     * @param timeout timeout to leave the channel if the channel is empty.
     */
    public void joinVoice(IPonyGuildable command, Consumer<PonySearchQuerry> onJoin, long timeout, String failMessage) {
        Member member = command.getFirstMentionedOrSender();
        GuildVoiceState state = member.getVoiceState();
        if (state == null) {
            return;
        }
        try {
            this.joinVoice(state.getChannel(), onJoin, timeout);
        }
        catch (JoinVoiceFailedException e) {
            command.getChannel().sendMessage(failMessage).queue();
        }
    }

    /**
     * joins a channel and instantly leaves if the channel is empty.
     *
     * @param channel channel to join
     * @param onJoin  action that sould be executed on join
     */
    public void joinVoice(VoiceChannel channel, Consumer<PonySearchQuerry> onJoin) throws JoinVoiceFailedException {
        this.joinVoice(channel, onJoin, 0);
    }

    /**
     * joins a channel and automaticly leaves if the channel is empty.
     * -1 == no autoleave;
     * 0 == instant leave
     * i > 0 leave after i milli seconds
     *
     * @param channel channel to join
     * @param onJoin  action that should be executed on join
     * @param timeout timeout to leave the channel if the channel is empty.
     */
    public void joinVoice(VoiceChannel channel, Consumer<PonySearchQuerry> onJoin, long timeout) throws JoinVoiceFailedException {
        if (channel == null) {
            throw new JoinVoiceFailedException("Bot can't Connected whit a Voice Channel because the Channel is null");
        }
        this.guildManager.openAudioConnection(channel);
        this.audioPlayer.setVolume(10);
        this.channel = channel;
        this.timeouts = new long[] { timeout, timeout };
        PonySearchQuerry querry = new PonySearchQuerry(this);
        onJoin.accept(querry);
    }

    public void moveBotToVoice(VoiceChannel channel) {
        this.moveBotToVoice(channel, null);
    }

    public void moveBotToVoice(VoiceChannel channel, Consumer<PonySearchQuerry> onJoin) {
        if (channel == null) {
            throw new JoinVoiceFailedException("Bot can't Connected whit a Voice Channel because the Channel is null");
        }
        this.guildManager.openAudioConnection(channel);
        this.channel = channel;
        this.timeouts[PonyAudioGuildController.TIMEOUT_COUNTER] = this.timeouts[PonyAudioGuildController.DEFAULT_TIMEOUT];
        if (onJoin != null) {
            PonySearchQuerry querry = new PonySearchQuerry(this);
            onJoin.accept(querry);
        }
    }



    public boolean leaveVoice() {
        if (this.channel == null) {
            return false;
        }
        this.guildManager.closeAudioConnection();
        this.channel = null;
        this.timeouts = new long[0];
        return true;
    }

    public void setSpeakVolume(int volume) {
        this.audioPlayer.setVolume(volume);
    }

    public void setAutoReconnected(boolean reconnected) {
        this.getGuildManager().setAutoReconnect(reconnected);
    }

    public void setNewTimeout(int timeout) {
        this.timeouts = new long[] { timeout, timeout };
    }

    public void resetTimeout() {
        this.timeouts[PonyAudioGuildController.TIMEOUT_COUNTER] = this.timeouts[PonyAudioGuildController.DEFAULT_TIMEOUT];
    }

    public void stopTrack() {
        this.audioPlayer.stopTrack();
    }

    public boolean pause() {
        if (this.isPaused()) {
            return false;
        }
        this.audioPlayer.setPaused(true);
        return true;
    }

    public boolean play() {
        if (!this.isPaused()) {
            return false;
        }
        this.audioPlayer.setPaused(false);
        return true;
    }

    public boolean isPaused() {
        return this.audioPlayer.isPaused();
    }

    public AudioTrack getPlayingTrack() {
        return this.audioPlayer.getPlayingTrack();
    }

    public void voiceTimeout() {
        if (this.timeouts[PonyAudioGuildController.TIMEOUT_COUNTER] == -1) {
            return;
        }
        new Thread(() -> {
            for (; this.timeouts[PonyAudioGuildController.TIMEOUT_COUNTER] > 0; this.timeouts[PonyAudioGuildController.TIMEOUT_COUNTER]--) {
                if (this.channel.getMembers().size() > 1) {
                    this.timeouts[PonyAudioGuildController.TIMEOUT_COUNTER] = this.timeouts[PonyAudioGuildController.DEFAULT_TIMEOUT];
                    return;
                }
            }
            this.guildManager.closeAudioConnection();
            this.channel = null;
            this.timeouts = new long[0];
        }).start();
    }

    public void loadItem(AudioTrackInfo info) {
        this.audioPlayerManager.loadItem(info.identifier, this.resultHandler);
    }

    public void loadItem(AudioTrack info) {
        this.audioPlayerManager.loadItem(info.getIdentifier(), this.resultHandler);
    }

    public void loadItem(String uri) {
        this.audioPlayerManager.loadItem(uri, this.resultHandler);
    }
}
