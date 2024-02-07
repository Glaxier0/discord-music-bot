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
import java.util.List;

@AllArgsConstructor
public class SwapCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MusicCommandUtils utils;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var ephemeralOption = event.getOption("ephemeral");
        boolean ephemeral = ephemeralOption == null || ephemeralOption.getAsBoolean();

        if (utils.channelControl(event)) {
            GuildMusicManager musicManager = playerManagerService.getMusicManager(event);
            List<AudioTrack> trackList = new ArrayList<>(musicManager.scheduler.queue);
            var firstOption = event.getOption("songnum1");
            var secondOption = event.getOption("songnum2");

            if (firstOption == null || secondOption == null) {
                event.replyEmbeds(new EmbedBuilder()
                                .setDescription("Song numbers can't be null.")
                                .setColor(Color.RED)
                                .build())
                        .setEphemeral(ephemeral)
                        .queue();
                return;
            }

            if (trackList.size() > 1) {
                int first = firstOption.getAsInt() - 1;
                int second = secondOption.getAsInt() - 1;

                try {
                    AudioTrack temp = trackList.get(first);
                    trackList.set(first, trackList.get(second));
                    trackList.set(second, temp);
                } catch (Exception e) {
                    event.replyEmbeds(new EmbedBuilder()
                                    .setDescription("Please enter a valid queue ids for both of the songs.")
                                    .setColor(Color.RED)
                                    .build())
                            .setEphemeral(ephemeral)
                            .queue();
                    return;
                }

                musicManager.scheduler.queue.clear();
                for (AudioTrack track : trackList) {
                    musicManager.scheduler.queue(track);
                }

                event.replyEmbeds(new EmbedBuilder()
                                .setDescription("Successfully swapped order of the two songs")
                                .setColor(Color.GREEN)
                                .build())
                        .setEphemeral(ephemeral)
                        .queue();
            } else if (trackList.size() == 1) {
                event.replyEmbeds(new EmbedBuilder()
                                .setDescription("There is only one song in queue.")
                                .setColor(Color.RED)
                                .build())
                        .setEphemeral(ephemeral)
                        .queue();
            } else {
                event.replyEmbeds(new EmbedBuilder()
                                .setDescription("Queue is empty.")
                                .setColor(Color.RED)
                                .build())
                        .setEphemeral(ephemeral)
                        .queue();
            }
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
