package com.discord.bot.commands.musiccommands;

import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.discord.bot.commands.ISlashCommand;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;


import java.awt.*;
import java.util.concurrent.BlockingQueue;

public class QueueCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;

    public QueueCommand(PlayerManagerService playerManagerService) {
        this.playerManagerService = playerManagerService;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        BlockingQueue<AudioTrack> queue = playerManagerService.getMusicManager(event).scheduler.queue;
        var trackList = queue.stream().toList();

        int totalPages = (int) Math.ceil((double) queue.size() / 20);

        var option = event.getOption("page");
        int page = 1;

        if (option != null) {
            page = option.getAsInt();
        }

        if (queue.isEmpty()) {
            embedBuilder.setDescription("The queue is currently empty").setColor(Color.RED);
            event.replyEmbeds(embedBuilder.build()).queue();
            return;
        }

        if (page < 1 || page > totalPages) {
            embedBuilder.setDescription("Invalid page number. Please enter a valid page number " +
                    "between 1 and " + totalPages).setColor(Color.RED);
            event.replyEmbeds(embedBuilder.build()).queue();
            return;
        }

        embedBuilder.setTitle("Queue - Page " + page);
        int startIndex = (page - 1) * 20;
        int endIndex = Math.min(startIndex + 20, queue.size());

        for (int i = startIndex; i < endIndex; i++) {
            AudioTrack track = trackList.get(i);
            AudioTrackInfo info = track.getInfo();
            embedBuilder.appendDescription((i + 1) + ". " + info.title + "\n");
        }

        event.replyEmbeds(embedBuilder.build()).addActionRow(
                Button.secondary("prev", "Previous Page")
                        .withDisabled(page == 1),
                Button.secondary("next", "Next Page")
                        .withDisabled(page == totalPages)).queue();
    }
}
