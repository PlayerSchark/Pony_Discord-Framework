package io.schark.pny.example.commands;

import io.schark.pny.example.AudioResultHandler;
import io.schark.pony.core.feat.audio.PonyAudioGuildController;
import io.schark.pony.core.feat.audio.PonySearchQuery;
import io.schark.pony.core.feat.commands.annotation.Alias;
import io.schark.pony.core.feat.commands.command.PonyChatCommand;
import io.schark.pony.core.feat.commands.command.PonyPublicChatCommand;
import io.schark.pony.core.feat.commands.executor.input.PonyChatCommandExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class PlayCommand extends PonyChatCommandExecutor {

    @Alias(aliases = { "p", "music", "musik" })
    public PlayCommand() {
        super("play", "Plays a song", true);
    }

    @Override
    public String executeCommand(PonyChatCommand cmd) {
        PonyPublicChatCommand command = (PonyPublicChatCommand) cmd;
        PonyAudioGuildController controller = PonyAudioGuildController.create(command, ctrl->new AudioResultHandler(ctrl, command));
        controller.joinVoice(command, this.search(command, controller), "I'm not in the same voice channel :/"); // timeout = 0 no delay | timeout = -1 no leave
        return null;
    }

    @NotNull private Consumer<PonySearchQuery> search(PonyPublicChatCommand command, PonyAudioGuildController controller) {
        return search -> search.searchYoutube(command.getArguments());
    }
}
