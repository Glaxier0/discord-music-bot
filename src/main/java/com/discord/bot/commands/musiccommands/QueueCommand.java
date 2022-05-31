package com.discord.bot.commands.musiccommands;

import com.discord.bot.commands.musiccommands.Fails.FailQueueEmptyStrategy;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class QueueCommand extends MusicPlayerCommand {

    public QueueCommand(PlayerManagerService playerManagerService, ChannelValidation channelValidation) {
        this.playerManagerService = playerManagerService;
        this.channelValidation = channelValidation;
        this.failDescriptionStrategy = new FailQueueEmptyStrategy();
    }
    
    @Override
    void operate(SlashCommandInteractionEvent event, EmbedBuilder embedBuilder) {
        playerManagerService.showTrackQueue(event, embedBuilder);
    }
    
    @Override
    boolean isValidState(SlashCommandInteractionEvent event) {
        return playerManagerService.isQueueNotEmpty(event);
    }

    
}
