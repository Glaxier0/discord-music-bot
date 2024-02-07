package com.discord.bot.commands.musiccommands;

import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.discord.bot.commands.ISlashCommand;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.concurrent.BlockingQueue;

@AllArgsConstructor
public class QueueCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MusicCommandUtils utils;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        boolean ephemeral = utils.isEphemeralOptionEnabled(event);
        BlockingQueue<AudioTrack> queue = playerManagerService.getMusicManager(event).scheduler.queue;
        var trackList = queue.stream().toList();

        if (queue.isEmpty()) {
            sendEmptyQueueResponse(event, embedBuilder, ephemeral);
            return;
        }

        int totalPages = (int) Math.ceil((double) queue.size() / 20);
        var pageOption = event.getOption("page");
        int page = pageOption == null ? 1 : Math.min(Math.max(pageOption.getAsInt(), 1), totalPages);

        if (page < 1 || page > totalPages) {
            sendInvalidPageResponse(event, embedBuilder, ephemeral, totalPages);
            return;
        }

        embedBuilder = utils.queueBuilder(embedBuilder, page, queue, trackList);

        event.replyEmbeds(embedBuilder.build()).addActionRow(
                        Button.secondary("prev", "Previous Page")
                                .withDisabled(page == 1),
                        Button.secondary("next", "Next Page")
                                .withDisabled(page == totalPages))
                .setEphemeral(ephemeral)
                .queue();
    }

    private void sendEmptyQueueResponse(SlashCommandInteractionEvent event, EmbedBuilder embedBuilder, boolean ephemeral) {
        embedBuilder.setDescription("The queue is currently empty").setColor(Color.RED);
        event.replyEmbeds(embedBuilder.build())
                .setEphemeral(ephemeral)
                .queue();
    }

    private void sendInvalidPageResponse(SlashCommandInteractionEvent event, EmbedBuilder embedBuilder, boolean ephemeral, int totalPages) {
        embedBuilder.setDescription("Invalid page number. Please enter a valid page number " +
                "between 1 and " + totalPages).setColor(Color.RED);
        event.replyEmbeds(embedBuilder.build())
                .setEphemeral(ephemeral)
                .queue();
    }
}