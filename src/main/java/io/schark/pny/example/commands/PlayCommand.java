package io.schark.pny.example.commands;

import io.schark.pny.example.AudioResult;
import io.schark.pony.core.feat.audio.PonyAudioGuildController;
import io.schark.pony.core.feat.commands.annotation.Alias;
import io.schark.pony.core.feat.commands.command.PonyChatCommand;
import io.schark.pony.core.feat.commands.command.PonyPublicChatCommand;
import io.schark.pony.core.feat.commands.executor.input.PonyChatCommandExecutor;

public class PlayCommand extends PonyChatCommandExecutor {

    @Alias(aliases = { "p", "music", "musik" })
    public PlayCommand() {
        super("play", "Plays a song", true);
    }

    @Override
    public String executeCommand(PonyChatCommand cmd) {
        PonyPublicChatCommand command = (PonyPublicChatCommand) cmd;
        PonyAudioGuildController controller = PonyAudioGuildController.create(command);

        controller.joinVoice(command, search -> search.searchYoutube(command.getStringArgument(0).getContent(), new AudioResult())
                        , 3_000); // timeout = 0 no delay | timeout = -1 no leave
        return "has start";
    }
}
