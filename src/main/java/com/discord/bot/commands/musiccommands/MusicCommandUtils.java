package com.discord.bot.commands.musiccommands;

import net.dv8tion.jda.api.entities.GuildVoiceState;

public class MusicCommandUtils {
    public boolean channelControl(GuildVoiceState selfVoiceState, GuildVoiceState memberVoiceState) {
        if (!selfVoiceState.inAudioChannel()) {
            return false;
        }
        if (!memberVoiceState.inAudioChannel()) {
            return false;
        }
        return memberVoiceState.getChannel() == selfVoiceState.getChannel();
    }
}
