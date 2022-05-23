package com.discord.bot.commands.musiccommands;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class LoopCommand extends MusicPlayerCommand {
    MusicCommandUtils utils;
    PlayerManagerService playerManagerService;
    public LoopCommand(PlayerManagerService playerManagerService, MusicCommandUtils utils) {
        this.playerManagerService = playerManagerService;
        this.utils = utils;
    }

    @Override
    void operate(SlashCommandInteractionEvent event, EmbedBuilder embedBuilder) {
        boolean repeat = playerManagerService.getMusicManager(event).scheduler.repeating;
        playerManagerService.getMusicManager(event).scheduler.repeating = !repeat;
        if (!repeat) {
            event.replyEmbeds(embedBuilder.setDescription(":white_check_mark: Track loop enabled.").setColor(Color.GREEN).build()).queue();
        } else {
            event.replyEmbeds(embedBuilder.setDescription(":x: Track loop disabled.").setColor(Color.RED).build()).queue();
        }
    }

    @Override
    boolean isValidState(SlashCommandInteractionEvent event) {
        return utils.isBotAndUserInSameChannel(event);
    }

    @Override
    String getFailDescription() {
        return "Please be in a same voice channel as bot.";
    }

}
