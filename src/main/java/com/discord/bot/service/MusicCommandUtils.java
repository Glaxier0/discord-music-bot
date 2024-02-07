package com.discord.bot.service;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;

@Service
public class MusicCommandUtils {
    public boolean channelControl(SlashCommandInteractionEvent event) {
        var guild = event.getGuild();

        if (guild != null && event.getMember() != null) {
            GuildVoiceState selfVoiceState = guild.getSelfMember().getVoiceState();
            GuildVoiceState memberVoiceState = event.getMember().getVoiceState();
            if (selfVoiceState != null && memberVoiceState != null) {
                if (!selfVoiceState.inAudioChannel()) {
                    return false;
                }
                if (!memberVoiceState.inAudioChannel()) {
                    return false;
                }

                return memberVoiceState.getChannel() == selfVoiceState.getChannel();
            }
        }
        return false;
    }

    public EmbedBuilder queueBuilder(EmbedBuilder embedBuilder, int page, BlockingQueue<AudioTrack> queue, List<AudioTrack> trackList) {
        embedBuilder.setTitle("Queue - Page " + page);
        int startIndex = (page - 1) * 20;
        int endIndex = Math.min(startIndex + 20, queue.size());

        for (int i = startIndex; i < endIndex; i++) {
            AudioTrack track = trackList.get(i);
            AudioTrackInfo info = track.getInfo();
            embedBuilder.appendDescription((i + 1) + ". " + info.title + "\n");
        }

        return embedBuilder;
    }

    public EmbedBuilder handleSingleCommand(SlashCommandInteractionEvent event, BlockingQueue<AudioTrack> queue, EmbedBuilder embedBuilder) {
        int index = Objects.requireNonNull(event.getOption("songnum")).getAsInt() - 1;

        if (index >= 0 && index < queue.size()) {
            var iterator = queue.iterator();

            for (int i = 0; i < index; i++) {
                iterator.next();
            }

            var removedSong = iterator.next();
            //noinspection ResultOfMethodCallIgnored
            queue.remove(removedSong);

            embedBuilder.setDescription("Song removed from the queue.").setColor(Color.GREEN);
        } else embedBuilder.setDescription("Invalid song index. Please provide a valid index.").setColor(Color.RED);

        return embedBuilder;
    }

    public EmbedBuilder handleBetweenCommand(SlashCommandInteractionEvent event, BlockingQueue<AudioTrack> queue, EmbedBuilder embedBuilder) {
        var firstIndex = Objects.requireNonNull(event.getOption("songnum1")).getAsInt() - 1;
        var lastIndex = Objects.requireNonNull(event.getOption("songnum2")).getAsInt() - 1;

        if (firstIndex >= 0 && lastIndex >= 0 && firstIndex <= lastIndex && lastIndex < queue.size()) {
            var iterator = queue.iterator();
            var songsToRemove = new ArrayList<>();

            for (int i = 0; i <= lastIndex; i++) {
                var song = iterator.next();
                if (i >= firstIndex) {
                    songsToRemove.add(song);
                }
            }
            //noinspection SuspiciousMethodCalls
            queue.removeAll(songsToRemove);

            embedBuilder.setDescription("Removed songs from the queue.").setColor(Color.GREEN);
        } else embedBuilder
                .setDescription("Indexes are not valid for the current queue. Please check song numbers again.")
                .setColor(Color.RED);

        return embedBuilder;
    }

    public EmbedBuilder handleAllCommand(BlockingQueue<AudioTrack> queue, EmbedBuilder embedBuilder) {
        //noinspection SuspiciousMethodCalls
        queue.removeAll(Arrays.asList(queue.toArray()));

        embedBuilder.setDescription("Removed songs from the queue.").setColor(Color.GREEN);

        return embedBuilder;
    }

    public boolean isEphemeralOptionEnabled(SlashCommandInteractionEvent event) {
        var ephemeralOption = event.getOption("ephemeral");
        return ephemeralOption == null || ephemeralOption.getAsBoolean();
    }
}