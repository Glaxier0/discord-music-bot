package com.discord.bot.commands.musiccommands;

import com.discord.bot.service.audioplayer.PlayerManagerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class SkipCommand extends MusicPlayerCommand {

    public SkipCommand(PlayerManagerService playerManagerService, ChannelValidation channelValidation) {
        this.playerManagerService = playerManagerService;
        this.channelValidation = channelValidation;
    }

    @Override
    void operate(SlashCommandInteractionEvent event, EmbedBuilder embedBuilder) {
        playerManagerService.skipToNextTrack(event, embedBuilder);
    }
    
    @Override
    boolean isValidState(SlashCommandInteractionEvent event) {
        return channelValidation.isValid(event);
    }

    @Override
    String getFailDescription() {
        return "Please be in a same voice channel as bot.";
    }
}
