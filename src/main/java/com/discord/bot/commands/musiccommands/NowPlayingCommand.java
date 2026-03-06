package com.discord.bot.commands.musiccommands;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.ReplyService;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import dev.arbjerg.lavalink.client.player.Track;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class NowPlayingCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MusicCommandUtils utils;
    ReplyService replyService;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (utils.channelControl(event)) {
            var musicManager = playerManagerService.getMusicManager(event.getGuild());
            var playerOpt = musicManager.getPlayer();

            if (playerOpt.isPresent()) {
                LavalinkPlayer player = playerOpt.get();
                Track track = player.getTrack();

                if (track != null) {
                    long durationMs = track.getInfo().getLength();
                    long positionMs = player.getPosition();

                    long durationSeconds = TimeUnit.MILLISECONDS.toSeconds(durationMs);
                    long hours = durationSeconds / 3600;
                    long minutes = (durationSeconds % 3600) / 60;
                    long seconds = durationSeconds % 60;

                    long remainingSeconds = durationSeconds - TimeUnit.MILLISECONDS.toSeconds(positionMs);
                    long remainingHours = remainingSeconds / 3600;
                    long remainingMinutes = (remainingSeconds % 3600) / 60;
                    long remainingSecs = remainingSeconds % 60;

                    var timestamp = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                    var remaining = String.format("%02d:%02d:%02d", remainingHours, remainingMinutes, remainingSecs);

                    embedBuilder.setTitle("Now playing")
                            .setDescription(":headphones: [" + track.getInfo().getTitle() + "](" + track.getInfo().getUri() + ")")
                            .addField(":watch: Timestamp", "```" + " " + timestamp + "```", true)
                            .addField(":stopwatch: Remaining", "```" + " " + remaining + "```", true)
                            .setColor(Color.GREEN);
                } else {
                    embedBuilder.setDescription("There is no song currently playing.").setColor(Color.RED);
                }
            } else {
                embedBuilder.setDescription("There is no song currently playing.").setColor(Color.RED);
            }
        } else embedBuilder.setDescription("Please be in a same voice channel as bot.").setColor(Color.RED);
        replyService.replyWithEmbed(event, embedBuilder, utils.isEphemeralOptionEnabled(event));
    }
}