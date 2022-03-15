package com.discord.bot.commands.musiccommands;

import com.discord.bot.audioplayer.PlayerManagerService;
import com.discord.bot.commands.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class ResumeCommand implements ISlashCommand {
    MusicCommandUtils utils;
    PlayerManagerService playerManagerService;

    public ResumeCommand(PlayerManagerService playerManagerService, MusicCommandUtils utils) {
        this.playerManagerService = playerManagerService;
        this.utils = utils;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (utils.channelControl(event)) {
            EmbedBuilder embedBuilder = new EmbedBuilder();

            playerManagerService.getMusicManager(event).audioPlayer.setPaused(false);
            event.replyEmbeds(embedBuilder.setDescription("Song resumed")
                    .setColor(Color.GREEN).build()).queue();
        } else {
            event.replyEmbeds(new EmbedBuilder().setDescription("Please be in a same voice channel as bot.")
                    .setColor(Color.RED).build()).queue();
        }
    }
}
