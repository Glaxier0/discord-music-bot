package com.discord.bot.commands.musiccommands;

import com.discord.bot.commands.musiccommands.Fails.ChannelFailStrategy;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class LoopCommand extends MusicPlayerCommand {
    ChannelValidation utils;
    PlayerManagerService playerManagerService;
    public LoopCommand(PlayerManagerService playerManagerService, ChannelValidation channelValidation) {
        this.playerManagerService = playerManagerService;
        this.utils = channelValidation;
        this.failDescriptionStrategy = new ChannelFailStrategy();
    }

    @Override
    void operate(SlashCommandInteractionEvent event, EmbedBuilder embedBuilder) {
        playerManagerService.toggleLoopCurrentTrack(event, embedBuilder);
    }

    @Override
    boolean isValidState(SlashCommandInteractionEvent event) {
        return utils.isValid(event);
    }
}
