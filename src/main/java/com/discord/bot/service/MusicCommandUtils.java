package com.discord.bot.service;

import com.discord.bot.audioplayer.GuildMusicManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.BlockingQueue;

@Component
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

    public boolean isEphemeralOptionEnabled(SlashCommandInteractionEvent event) {
        var ephemeralOption = event.getOption("ephemeral");
        return ephemeralOption == null || ephemeralOption.getAsBoolean();
    }

    public void playerCleaner(GuildMusicManager musicManager) {
        musicManager.scheduler.repeating = false;
        musicManager.scheduler.player.setPaused(false);
        musicManager.scheduler.player.stopTrack();
        musicManager.scheduler.queue.clear();
    }
}