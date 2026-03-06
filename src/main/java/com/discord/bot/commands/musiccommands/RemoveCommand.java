package com.discord.bot.commands.musiccommands;

import com.discord.bot.audioplayer.GuildMusicManager;
import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.ReplyService;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import dev.arbjerg.lavalink.client.player.Track;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Queue;

@AllArgsConstructor
public class RemoveCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MusicCommandUtils utils;
    ReplyService replyService;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (utils.channelControl(event)) {
            GuildMusicManager musicManager = playerManagerService.getMusicManager(event.getGuild());
            var queue = musicManager.getScheduler().queue;

            if (!queue.isEmpty()) {
                var command = event.getSubcommandName();

                if (command != null) {
                    switch (command) {
                        case "single" -> handleSingleCommand(event, queue, embedBuilder);
                        case "between" -> handleBetweenCommand(event, queue, embedBuilder);
                        case "all" -> handleAllCommand(queue, embedBuilder);
                    }
                } else embedBuilder.setDescription("Please specify subcommand.").setColor(Color.RED);
            } else embedBuilder.setDescription("Song queue is empty.").setColor(Color.RED);
        } else embedBuilder.setDescription("Please be in a same voice channel as bot.").setColor(Color.RED);

        replyService.replyWithEmbed(event, embedBuilder, utils.isEphemeralOptionEnabled(event));
    }

    private void handleSingleCommand(SlashCommandInteractionEvent event, Queue<Track> queue, EmbedBuilder embedBuilder) {
        int index = Objects.requireNonNull(event.getOption("songnum")).getAsInt() - 1;

        if (index >= 0 && index < queue.size()) {
            var trackList = new ArrayList<>(queue);
            trackList.remove(index);
            queue.clear();
            queue.addAll(trackList);

            embedBuilder.setDescription("Song removed from the queue.").setColor(Color.GREEN);
        } else embedBuilder.setDescription("Invalid song index. Please provide a valid index.").setColor(Color.RED);
    }

    private void handleBetweenCommand(SlashCommandInteractionEvent event, Queue<Track> queue, EmbedBuilder embedBuilder) {
        var firstIndex = Objects.requireNonNull(event.getOption("songnum1")).getAsInt() - 1;
        var lastIndex = Objects.requireNonNull(event.getOption("songnum2")).getAsInt() - 1;

        if (firstIndex >= 0 && lastIndex >= 0 && firstIndex <= lastIndex && lastIndex < queue.size()) {
            var trackList = new ArrayList<>(queue);
            trackList.subList(firstIndex, lastIndex + 1).clear();
            queue.clear();
            queue.addAll(trackList);

            embedBuilder.setDescription("Removed songs from the queue.").setColor(Color.GREEN);
        } else embedBuilder
                .setDescription("Indexes are not valid for the current queue. Please check song numbers again.")
                .setColor(Color.RED);
    }

    private void handleAllCommand(Queue<Track> queue, EmbedBuilder embedBuilder) {
        queue.clear();
        embedBuilder.setDescription("Removed songs from the queue.").setColor(Color.GREEN);
    }
}