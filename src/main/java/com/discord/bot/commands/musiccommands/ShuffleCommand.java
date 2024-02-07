package com.discord.bot.commands.musiccommands;

import com.discord.bot.audioplayer.GuildMusicManager;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.discord.bot.commands.ISlashCommand;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class ShuffleCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MusicCommandUtils utils;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var ephemeralOption = event.getOption("ephemeral");
        boolean ephemeral = ephemeralOption == null || ephemeralOption.getAsBoolean();

        if (utils.channelControl(event)) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            GuildMusicManager musicManager = playerManagerService.getMusicManager(event);

            List<AudioTrack> trackList = new ArrayList<>(musicManager.scheduler.queue);
            if (trackList.size() > 1) {
                Collections.shuffle(trackList);
                musicManager.scheduler.queue.clear();

                for (AudioTrack track : trackList) {
                    musicManager.scheduler.queue(track);
                }

                embedBuilder.setDescription("Queue shuffled").setColor(Color.GREEN);
            } else {
                embedBuilder.setDescription("Queue size have to be at least two.").setColor(Color.RED);
            }
            event.replyEmbeds(embedBuilder
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
