package com.discord.bot.service;

import com.discord.bot.audioplayer.GuildMusicManager;
import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Queue;

@Component
public class MusicCommandUtils {
    public boolean channelControl(SlashCommandInteractionEvent event) {
        var guild = event.getGuild();
        var member = event.getMember();

        if (guild != null && member != null) {
            GuildVoiceState selfVoiceState = guild.getSelfMember().getVoiceState();
            GuildVoiceState memberVoiceState = member.getVoiceState();

            if (selfVoiceState != null && memberVoiceState != null) {
                if (!selfVoiceState.inAudioChannel() || !memberVoiceState.inAudioChannel()) {
                    return false;
                }

                return memberVoiceState.getChannel() == selfVoiceState.getChannel();
            }
        }
        return false;
    }

    public EmbedBuilder queueBuilder(EmbedBuilder embedBuilder, int page, Queue<Track> queue,
            List<Track> trackList) {
        embedBuilder.setTitle("Queue - Page " + page);
        int startIndex = (page - 1) * 20;
        int endIndex = Math.min(startIndex + 20, queue.size());

        for (int i = startIndex; i < endIndex; i++) {
            Track track = trackList.get(i);
            var info = track.getInfo();
            embedBuilder.appendDescription((i + 1) + ". " + info.getTitle() + "\n");
        }

        return embedBuilder;
    }

    public void leaveIfEmpty(Guild guild, GuildMusicManager musicManager) {
        if (musicManager.getScheduler().queue.isEmpty()) {
            musicManager.getPlayer().ifPresentOrElse(
                    (player) -> {
                        if (player.getTrack() == null) {
                            playerCleaner(musicManager);
                            guild.getAudioManager().closeAudioConnection();
                        }
                    },
                    () -> {
                        playerCleaner(musicManager);
                        guild.getAudioManager().closeAudioConnection();
                    }
            );
        }
    }

    public boolean isEphemeralOptionEnabled(SlashCommandInteractionEvent event) {
        var ephemeralOption = event.getOption("ephemeral");
        return ephemeralOption == null || ephemeralOption.getAsBoolean();
    }

    public Attachment getAttachedFile(OptionMapping fileOption) {
        return fileOption.getAsAttachment();
    }

    public void playerCleaner(GuildMusicManager musicManager) {
        musicManager.getScheduler().repeating = false;
        musicManager.getScheduler().queue.clear();
        musicManager.stop();
    }
}