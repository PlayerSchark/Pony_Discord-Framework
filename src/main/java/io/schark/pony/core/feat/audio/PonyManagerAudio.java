package io.schark.pony.core.feat.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import io.schark.pony.core.PonyManager;
import io.schark.pony.core.PonyManagerType;
import io.schark.pony.core.feat.audio.listener.VoiceLeaveListener;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Getter
public class PonyManagerAudio extends PonyManager {

    private AudioPlayerManager audioPlayerManager;
    private final Pattern youtubeRegex = Pattern.compile("^((?:https?:)?//)?((?:www|m)\\.)?((?:youtube\\.com|youtu.be))(/(?:[\\w\\-]+\\?v=|embed/|v/)?)([\\w\\-]+)(\\S+)?$");
    private final Pattern spotifyRegex = Pattern.compile("^(https://open.spotify.com/user/spotify/playlist/|spotify:user:spotify:playlist:)([a-zA-Z0-9]+)(.*)$");
    private Map<Guild, PonyAudioGuildController> audioGuildList;

    @Override
    public void init() {
        this.audioGuildList = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        PonyManagerType.LISTENER.manager().registerListener(new VoiceLeaveListener());
    }

    public PonyAudioGuildController createGuildAudio(Guild guild)  {
        if (this.audioGuildList.containsKey(guild)) {
            return this.audioGuildList.get(guild);
        }
        PonyAudioGuildController controller = new PonyAudioGuildController(guild, this.audioPlayerManager);
        this.audioGuildList.put(guild, controller);
        return controller;
    }

    public PonyAudioGuildController getController(Guild guild) {
        return this.audioGuildList.get(guild);
    }
}
