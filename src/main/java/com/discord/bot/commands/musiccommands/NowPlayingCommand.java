package com.discord.bot.commands.musiccommands;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class NowPlayingCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MusicCommandUtils utils;

    public NowPlayingCommand(PlayerManagerService playerManagerService, MusicCommandUtils utils) {
        this.playerManagerService = playerManagerService;
        this.utils = utils;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (utils.channelControl(event)) {
            AudioTrack track = playerManagerService.getMusicManager(event).audioPlayer.getPlayingTrack();

            if (track != null) {
                long durationSeconds = TimeUnit.MILLISECONDS.toSeconds(track.getDuration());
                long hours = durationSeconds / 3600;
                long minutes = (durationSeconds % 3600) / 60;
                long seconds = durationSeconds % 60;

                long remainingSeconds = durationSeconds - (TimeUnit.MILLISECONDS.toSeconds(track.getPosition()));
                long remainingHours = remainingSeconds / 3600;
                long remainingMinutes = (remainingSeconds % 3600) / 60;
                long remainingSecs = remainingSeconds % 60;

                var timestamp = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                var remaining = String.format("%02d:%02d:%02d", remainingHours, remainingMinutes, remainingSecs);

                embedBuilder.setTitle("Now playing").setDescription(":headphones: [" + track.getInfo().title
                                + "](" + track.getInfo().uri + ")")
                        .addField(":watch: Timestamp",  "```" + " " + timestamp + "```", true)
                        .addField(":stopwatch: Remaining", "```" + " " + remaining + "```", true)
                        .setColor(Color.GREEN);
            } else {
                embedBuilder.setDescription("There is no song currently playing.").setColor(Color.RED);
            }
        } else {
            embedBuilder.setDescription("Please be in a same voice channel as bot.").setColor(Color.RED);
        }
        event.replyEmbeds(embedBuilder.build()).queue();
    }
}
