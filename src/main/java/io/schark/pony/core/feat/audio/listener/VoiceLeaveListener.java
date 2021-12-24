package io.schark.pony.core.feat.audio.listener;

import io.schark.pony.Pony;
import io.schark.pony.core.PonyManagerType;
import io.schark.pony.core.feat.audio.PonyAudioGuildController;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.List;

public class VoiceLeaveListener extends ListenerAdapter {

    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent e) {
        this.checkBotLeave(e.getMember(), e.getChannelLeft().getMembers());
    }

    @Override
    public void onGuildVoiceMove(@Nonnull GuildVoiceMoveEvent e) {
        this.checkBotLeave(e.getMember(), e.getChannelLeft().getMembers());
    }

    private void checkBotLeave(Member leftMember, List<Member> members) {
        if (leftMember.getIdLong() == Pony.getInstance().getPonyBot().getId()) {
            return;
        }
        if (members.size() == 1) {
            PonyAudioGuildController controller = PonyManagerType.AUDIO.getManager().getController(leftMember.getGuild());
            controller.voiceTimeout();
        }
    }
}
