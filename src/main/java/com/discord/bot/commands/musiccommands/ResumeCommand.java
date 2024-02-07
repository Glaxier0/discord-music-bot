package com.discord.bot.commands.musiccommands;

import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.discord.bot.commands.ISlashCommand;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

@AllArgsConstructor
public class ResumeCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MusicCommandUtils utils;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var ephemeralOption = event.getOption("ephemeral");
        boolean ephemeral = ephemeralOption == null || ephemeralOption.getAsBoolean();

        if (utils.channelControl(event)) {
            EmbedBuilder embedBuilder = new EmbedBuilder();

            playerManagerService.getMusicManager(event).audioPlayer.setPaused(false);
            event.replyEmbeds(embedBuilder
                            .setDescription("Song resumed")
                            .setColor(Color.GREEN)
                            .build())
                    .setEphemeral(ephemeral)
                    .queue();
        } else {
            event.replyEmbeds(new EmbedBuilder()
                            .setDescription("Please be in a same voice channel as bot.")
                            .setColor(Color.RED)
                            .build())
                    .setEphemeral(ephemeral)
                    .queue();
        }
    }
}
