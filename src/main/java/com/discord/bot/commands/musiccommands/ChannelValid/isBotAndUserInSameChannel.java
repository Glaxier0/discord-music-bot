package com.discord.bot.commands.musiccommands.ChannelValid;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class isBotAndUserInSameChannel implements ValidStrategy{
    @Override
    public boolean isValid(SlashCommandInteractionEvent event) {
        if (!event.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {
            return false;
        }
        if (!event.getMember().getVoiceState().inAudioChannel()) {
            return false;
        }
        GuildVoiceState selfVoiceState = event.getGuild().getSelfMember().getVoiceState();
        GuildVoiceState botVoiceState = event.getMember().getVoiceState();
        return selfVoiceState.getChannel() == botVoiceState.getChannel();
    }
}
