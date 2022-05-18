package com.discord.bot.commands.musiccommands;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class MusicCommandUtils {
    public boolean channelControl(SlashCommandInteractionEvent event) {
        GuildVoiceState selfVoiceState = event.getGuild().getSelfMember().getVoiceState();
        GuildVoiceState memberVoiceState = event.getMember().getVoiceState();

        if (!selfVoiceState.inAudioChannel()) {
            return false;
        }
        if (!memberVoiceState.inAudioChannel()) {
            return false;
        }
        return memberVoiceState.getChannel() == selfVoiceState.getChannel();
    }

    public boolean isBotInVoiceChannel(SlashCommandInteractionEvent event) {
        return event.getGuild().getSelfMember().getVoiceState().inAudioChannel();
    }

    public boolean isUserInVoiceChannel(SlashCommandInteractionEvent event) {
        return event.getMember().getVoiceState().inAudioChannel();
    }
}
