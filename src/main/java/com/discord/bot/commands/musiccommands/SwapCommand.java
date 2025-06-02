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
        EmbedBuilder embedBuilder = new EmbedBuilder();
        var ephemeralOption = event.getOption("ephemeral");
        boolean ephemeral = ephemeralOption == null || ephemeralOption.getAsBoolean();

        if (utils.channelControl(event)) {
            GuildMusicManager musicManager = playerManagerService.getMusicManager(event.getGuild());
            List<AudioTrack> trackList = new ArrayList<>(musicManager.scheduler.queue);
            var firstOption = event.getOption("songnum1");
            var secondOption = event.getOption("songnum2");

            if (firstOption == null || secondOption == null) {
                embedBuilder.setDescription("Both song numbers must be provided.").setColor(Color.RED);
            } else {
                int first = firstOption.getAsInt() - 1;
                int second = secondOption.getAsInt() - 1;
                int size = musicManager.scheduler.queue.size();

                if (first >= size || second >= size || first < 0 || second < 0) {
                    embedBuilder.setDescription("Please enter valid queue positions for both songs.")
                            .setColor(Color.RED);
                } else if (trackList.size() > 1) {
                    AudioTrack temp = trackList.get(first);
                    trackList.set(first, trackList.get(second));
                    trackList.set(second, temp);

                    musicManager.scheduler.queue.clear();
                    musicManager.scheduler.queueAll(trackList);

                    embedBuilder.setDescription("Successfully swapped the order of the two songs.")
                            .setColor(Color.GREEN);
                } else if (trackList.size() == 1) {
                    embedBuilder.setDescription("There is only one song in the queue.").setColor(Color.RED);
                } else {
                    embedBuilder.setDescription("Queue is empty.").setColor(Color.RED);
                }
            }
        } else {
            embedBuilder.setDescription("Please be in the same voice channel as the bot.").setColor(Color.RED);
        }

        event.replyEmbeds(embedBuilder.build()).setEphemeral(ephemeral).queue();
    }
}