package com.discord.bot.commands.musiccommands;

import com.discord.bot.service.audioplayer.PlayerManagerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class SkipCommand extends MusicPlayerCommand {

    public SkipCommand(PlayerManagerService playerManagerService, MusicCommandUtils utils) {
        this.playerManagerService = playerManagerService;
        this.utils = utils;
    }

    @Override
    void operate(SlashCommandInteractionEvent event, EmbedBuilder embedBuilder) {
        playerManagerService.getMusicManager(event).scheduler.nextTrack();
        event.replyEmbeds(embedBuilder.setDescription("Song skipped").setColor(Color.GREEN).build()).queue();
    }

    @Override
    boolean isValidState(SlashCommandInteractionEvent event) {
        return utils.isValid(event);
    }

    @Override
    String getFailDescription() {
        return "Please be in a same voice channel as bot.";
    }
}
