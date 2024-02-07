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
            GuildMusicManager musicManager = playerManagerService.getMusicManager(event);
            List<AudioTrack> trackList = new ArrayList<>(musicManager.scheduler.queue);
            var firstOption = event.getOption("songnum1");
            var secondOption = event.getOption("songnum2");
            int first = firstOption.getAsInt() - 1;
            int second = secondOption.getAsInt() - 1;
            int size = musicManager.scheduler.queue.size();

            if (firstOption == null || secondOption == null) {
                embedBuilder.setDescription("Song numbers can't be null.").setColor(Color.RED);
            } else if (first >= size || second >= size) {
                embedBuilder.setDescription("Please enter a valid queue ids for both of the songs.").setColor(Color.RED);
            } else {
                if (trackList.size() > 1) {
                    AudioTrack temp = trackList.get(first);
                    trackList.set(first, trackList.get(second));
                    trackList.set(second, temp);

                    musicManager.scheduler.queue.clear();
                    musicManager.scheduler.queueAll(trackList);

                    embedBuilder.setDescription("Successfully swapped order of the two songs").setColor(Color.GREEN);
                } else if (trackList.size() == 1) {
                    embedBuilder.setDescription("There is only one song in queue.").setColor(Color.RED);
                } else embedBuilder.setDescription("Queue is empty.").setColor(Color.RED);
            }
        } else embedBuilder.setDescription("Please be in a same voice channel as bot.").setColor(Color.RED);

        event.replyEmbeds(embedBuilder.build()).setEphemeral(ephemeral).queue();
    }
}