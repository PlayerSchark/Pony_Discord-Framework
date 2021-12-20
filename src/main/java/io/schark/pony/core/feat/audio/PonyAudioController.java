package io.schark.pony.core.feat.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.schark.pony.Pony;
import io.schark.pony.core.feat.audio.handler.AudioSendPlayerHandler;
import io.schark.pony.core.feat.commands.in.PonyArg;
import io.schark.pony.exception.JoinVoiceFailedException;
import io.schark.pony.exception.MusicSearchNoMatchesException;
import io.schark.pony.exception.MusicSearchURLException;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

@Getter
public class PonyAudioController {

    private final Guild guild;
    private final AudioPlayer player;
    private final AudioPlayerManager audioPlayerManager;
    private final AudioManager guildManager;

    public PonyAudioController(Guild guild) {
        this.guild = guild;
        this.audioPlayerManager = Pony.getInstance().getAudioPlayerManager();
        this.player = audioPlayerManager.createPlayer();
        this.guildManager = this.guild.getAudioManager();
        this.guildManager.setSendingHandler(new AudioSendPlayerHandler(player));
    }

    /**
     *
     * @param channelMember
     * @return ture - The Bot has joined the Voice
     *         False - Joining has failed
     */
    public void joinVoice(Member channelMember) throws JoinVoiceFailedException {
        GuildVoiceState state = channelMember.getVoiceState();
        if (state == null) {
            throw new JoinVoiceFailedException("Pony - Bot can´t not Connected whit a Voice Channel because the VoiceState is null");
        }
        this.joinVoice(state.getChannel());
    }

    public void joinVoice(VoiceChannel channel) throws JoinVoiceFailedException {
        //Nicklas this.autoLeave()
        if (channel == null) {
            throw new JoinVoiceFailedException("Pony - Bot can´t not Connected whit a Voice Channel because the Channel is null");
        }
        this.guildManager.openAudioConnection(channel);
    }

    public void searchYoutube(PonyArg arg) throws MusicSearchURLException, FriendlyException {
        if (!arg.getContent().startsWith("http://") && !arg.getContentRaw().startsWith("https://")) {
            throw new MusicSearchURLException("Pony - The Argument start no with Http");
        }
        String url = "ytsearch:" + arg.getContentRaw();
        audioPlayerManager.loadItem(url, new AudioLoadResultHandler()  {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {

            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {

            }

            @SneakyThrows
            @Override
            public void noMatches() {
                throw new MusicSearchNoMatchesException();
            }

            @Override
            public void loadFailed(FriendlyException e) {
                throw e;
            }
        });
    }

    private void autoLeave() {

    }


}
