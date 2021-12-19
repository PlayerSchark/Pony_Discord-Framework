package io.schark.pony.core.feat.commands.executor;

import io.schark.pony.core.feat.commands.command.IPonyDiscordCommand;
import io.schark.pony.core.feat.commands.command.PonyDiscordCommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Player_Schark
 */
public interface PonyDiscordCommandExecutable extends PonyCommandExecutable {

	default void afterExecute(IPonyDiscordCommand command, String result) {
		RestAction action = result == null || result.isEmpty() ? command.getHook().deleteOriginal() : command.getEvent().reply(result);
		action.queue();
	}

	@NotNull default CommandData getCommandData() {
		return new CommandData(this.getRawLabel(), this.getDescription());
	}

	@NotNull default OptionData[] withOptionData() {
		return new OptionData[0];
	}

	@Nullable default OptionData withSingleOptionData() {
		return null;
	}

	String executeCommand(PonyDiscordCommand command);

}
