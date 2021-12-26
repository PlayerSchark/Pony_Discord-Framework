package io.schark.pny.example.commands;

import io.schark.pny.example.AudioResult;
import io.schark.pony.core.feat.audio.PonyAudioGuildController;
import io.schark.pony.core.feat.commands.annotation.Alias;
import io.schark.pony.core.feat.commands.command.PonyChatCommand;
import io.schark.pony.core.feat.commands.executor.input.PonyChatCommandExecutor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class PlayCommand extends PonyChatCommandExecutor {

    @Alias(aliases = { "p", "music", "musik" })
    public PlayCommand() {
        super("play", "Plays a song", true);
    }

    @Override
    public String executeCommand(PonyChatCommand command) {

        Guild guild = command.getMessage().getGuild();
        VoiceChannel channel = guild.getVoiceChannelById(841030060404375552L); //TEST Voice Channel (Team Voice)
        PonyAudioGuildController controller = PonyAudioGuildController.create(guild);

        controller.joinVoice(channel, search -> search.searchYoutube(command.getArgument(0), new AudioResult())
                , 3_000); // timeout = 0 no delay | timeout = -1 no leave
        return "has start";
    }
}
