package io.schark.pony.core.feat.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import io.schark.pony.core.PonyManagerType;
import io.schark.pony.core.feat.commands.in.PonyArg;
import io.schark.pony.exception.MusicSearchURLException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PonySearchQuarry {

    private final AudioPlayerManager audioPlayerManager;

    public void searchYoutube(PonyArg<String> arg, PonyAudioResult result) {
        String content = arg.getContent();
        if (!content.startsWith("http://") && !content.startsWith("https://")) {
            throw new MusicSearchURLException("The Argument start no with 'http(s)://'");
        }
        if (!PonyManagerType.AUDIO.manager().getYoutubeRegex().matcher(content).find()) {
            throw new MusicSearchURLException("The Argument is no Youtube URL");
        }
        String url = "ytsearch:" + content;
        System.out.println("before loadItem");
        this.getAudioPlayerManager().loadItem(url, result);
        System.out.println("after loadItem");
    }
}
