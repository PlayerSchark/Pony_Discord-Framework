package io.schark.pony.core.feat.commands.executor;

import io.schark.pony.core.feat.commands.annotation.impl.PonyAccessor;
import io.schark.pony.core.feat.commands.command.PonyGuildCommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Player_Schark
 */
public interface PonyGuildCommandExecutable extends PonyCommandExecutable {

	String executeCommand(PonyGuildCommand command);

	long[] guildIds();
	default Class<? extends PonyAccessor> accessor() {
		return PonyAccessor.class;
	}

	default void afterExecute(PonyGuildCommand command, String result) {
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
}
