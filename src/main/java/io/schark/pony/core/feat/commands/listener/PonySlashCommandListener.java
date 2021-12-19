package io.schark.pony.core.feat.commands.listener;

import io.schark.pony.core.feat.commands.PonyManagerCommand;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * @author Player_Schark
 */
@RequiredArgsConstructor
public class PonySlashCommandListener extends ListenerAdapter {


	private final PonyManagerCommand manager;

	@Override
	public void onSlashCommand(@NotNull SlashCommandEvent e) {
	}
}
