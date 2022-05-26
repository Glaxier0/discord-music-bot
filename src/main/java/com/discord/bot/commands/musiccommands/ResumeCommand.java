package com.discord.bot.commands.musiccommands;

import com.discord.bot.commands.musiccommands.Fails.ChannelFailStrategy;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class ResumeCommand extends MusicPlayerCommand {

    public ResumeCommand(PlayerManagerService playerManagerService, MusicCommandUtils utils) {
        this.playerManagerService = playerManagerService;
        this.utils = utils;
        this.failDescriptionStrategy = new ChannelFailStrategy();
    }

    @Override
    void operate(SlashCommandInteractionEvent event, EmbedBuilder embedBuilder) {
        playerManagerService.getMusicManager(event).audioPlayer.setPaused(false);
        event.replyEmbeds(embedBuilder.setDescription("Song resumed")
                .setColor(Color.GREEN).build()).queue();
    }

    @Override
    boolean isValidState(SlashCommandInteractionEvent event) {
        return utils.isBotAndUserInSameChannel(event);
    }
}
