package com.discord.bot.commands.musiccommands;

import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.discord.bot.commands.ISlashCommand;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class QueueCommand implements ISlashCommand {
    MusicCommandUtils utils;
    PlayerManagerService playerManagerService;

    public QueueCommand(PlayerManagerService playerManagerService, MusicCommandUtils utils) {
        this.playerManagerService = playerManagerService;
        this.utils = utils;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        BlockingQueue<AudioTrack> queue = playerManagerService.getMusicManager(event).scheduler.queue;
        int trackCount = Math.min(queue.size(), 20);
        List<AudioTrack> trackList = new ArrayList<>(queue);

        if (queue.isEmpty()) {
            embedBuilder.setDescription("The queue is currently empty").setColor(Color.RED);
            event.replyEmbeds(embedBuilder.build()).queue();
            return;
        }

        embedBuilder.setTitle("Current Queue:");
        for (int i = 0; i < trackCount; i++) {
            AudioTrack track = trackList.get(i);
            AudioTrackInfo info = track.getInfo();
            embedBuilder.appendDescription((i + 1) + ". " + info.title + "\n");
        }

        if (trackList.size() > trackCount) {
            embedBuilder.appendDescription("And " + (trackList.size() - trackCount) + " more...");
        }

        event.replyEmbeds(embedBuilder.build()).queue();
    }
}
