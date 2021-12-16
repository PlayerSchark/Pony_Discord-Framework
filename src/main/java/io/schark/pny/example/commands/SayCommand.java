package io.schark.pny.example.commands;

import io.schark.pny.example.MyRoleEnum;
import io.schark.pony.core.feat.commands.annotation.Alias;
import io.schark.pony.core.feat.commands.annotation.impl.PonyAccessor;
import io.schark.pony.core.feat.commands.command.PonyCommand;
import io.schark.pony.core.feat.commands.executor.PonyChatCommandExecutor;
import io.schark.pony.core.feat.commands.executor.PonyCommandExecutor;

/**
 * @author Player_Schark
 */
public class SayCommand extends PonyChatCommandExecutor implements PonyAccessor {

	@Alias(aliases = {"tell", "announce", "sai"})
	//@AllowedRoles(ids = 15L)
	public SayCommand() {
		super("say");
	}

	@Override public String execute(PonyCommand command) {
		return null;
	}

	@Override public Long[] getIds(PonyCommandExecutor executor) {
		return new Long[] {
			MyRoleEnum.ADMIN.getId(),
			MyRoleEnum.MOD.getId()
		};
	}
}
