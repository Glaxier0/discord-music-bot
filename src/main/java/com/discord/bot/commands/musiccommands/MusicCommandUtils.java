package com.discord.bot.commands.musiccommands;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class MusicCommandUtils {
    public boolean channelControl(SlashCommandInteractionEvent event) {
        var guild = event.getGuild();

        if (guild != null && event.getMember() != null) {
            GuildVoiceState selfVoiceState = guild.getSelfMember().getVoiceState();
            GuildVoiceState memberVoiceState = event.getMember().getVoiceState();
            if (selfVoiceState != null && memberVoiceState != null) {
                if (!selfVoiceState.inAudioChannel()) {
                    return false;
                }
                if (!memberVoiceState.inAudioChannel()) {
                    return false;
                }

                return memberVoiceState.getChannel() == selfVoiceState.getChannel();
            }
        }
        return false;
    }
}
