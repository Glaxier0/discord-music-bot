package com.discord.bot.commands.musiccommands;

import com.discord.bot.audioplayer.GuildMusicManager;
import com.discord.bot.commands.musiccommands.Fails.ChannelFailStrategy;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.*;

public class LeaveCommand extends MusicPlayerCommand {
    public LeaveCommand(PlayerManagerService playerManagerService, MusicCommandUtils utils) {
        this.playerManagerService = playerManagerService;
        this.utils = utils;
        this.failDescriptionStrategy = new ChannelFailStrategy();
    }

    @Override
    void operate(SlashCommandInteractionEvent event, EmbedBuilder embedBuilder) {
        GuildMusicManager musicManager = playerManagerService.getMusicManager(event);
        AudioManager audioManager = event.getGuild().getAudioManager();
        musicManager.scheduler.player.stopTrack();
        musicManager.scheduler.queue.clear();
        audioManager.closeAudioConnection();
        event.replyEmbeds(embedBuilder.setDescription("Bye.").build()).queue();
    }

    @Override
    boolean isValidState(SlashCommandInteractionEvent event) {
        return utils.isValid(event);
    }
}
