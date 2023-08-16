package com.discord.bot.commands.musiccommands;

import com.discord.bot.commands.IButtonInteraction;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.concurrent.BlockingQueue;

public class QueueButton implements IButtonInteraction {
    PlayerManagerService playerManagerService;

    public QueueButton(PlayerManagerService playerManagerService) {
        this.playerManagerService = playerManagerService;
    }

    @Override
    public void click(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("prev")) {
            handlePreviousButtonClick(event);
        } else if (event.getComponentId().equals("next")) {
            handleNextButtonClick(event);
        }
    }

    private void handlePreviousButtonClick(ButtonInteractionEvent event) {
        var embed = event.getMessage().getEmbeds().stream().findFirst();

        var title = "Page 1";

        if (embed.isPresent()) {
            title = embed.get().getTitle() == null ? "Page 1" : embed.get().getTitle();
        }

        int currentPage = Integer.parseInt(title.substring(title.lastIndexOf("Page")
                + "Page".length()).trim());

        BlockingQueue<AudioTrack> queue = playerManagerService.getMusicManager(event).scheduler.queue;

        int pageSize = 20;
        int totalTracks = queue.size();
        int totalPages = (int) Math.ceil((double) totalTracks / pageSize);

        int previousPage = currentPage - 1;

        updateEmbed(event, totalPages, previousPage);
    }

    private void handleNextButtonClick(ButtonInteractionEvent event) {
        var embed = event.getMessage().getEmbeds().stream().findFirst();

        var title = "Page 1";

        if (embed.isPresent()) {
            title = embed.get().getTitle() == null ? "Page 1" : embed.get().getTitle();
        }

        int currentPage = Integer.parseInt(title.substring(title.lastIndexOf("Page")
                + "Page".length()).trim());

        BlockingQueue<AudioTrack> queue = playerManagerService.getMusicManager(event).scheduler.queue;

        int pageSize = 20;
        int totalTracks = queue.size();
        int totalPages = (int) Math.ceil((double) totalTracks / pageSize);

        int nextPage = currentPage + 1;

        updateEmbed(event, totalPages, nextPage);
    }

    private void updateEmbed(ButtonInteractionEvent event, int totalPages, int page) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        BlockingQueue<AudioTrack> queue = playerManagerService.getMusicManager(event).scheduler.queue;
        var trackList = queue.stream().toList();

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

        event.editMessageEmbeds(embedBuilder.build()).setActionRow(
                Button.secondary("prev", "Previous Page")
                        .withDisabled(page == 1),
                Button.secondary("next", "Next Page")
                        .withDisabled(page == totalPages)).queue();
    }
}
