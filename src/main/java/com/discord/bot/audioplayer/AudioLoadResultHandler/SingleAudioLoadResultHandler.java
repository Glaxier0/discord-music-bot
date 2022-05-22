package com.discord.bot.audioplayer.AudioLoadResultHandler;

import com.discord.bot.audioplayer.GuildMusicManager;
import com.discord.bot.entity.pojo.MusicPojo;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.List;

public class SingleAudioLoadResultHandler implements AudioLoadResultHandler {
    private final PlayerManagerService playerManagerService;
    private final EmbedBuilder embedBuilder;
    private final SlashCommandInteractionEvent event;
    private final GuildMusicManager musicManager;
    private final MusicPojo musicPojo;

    public SingleAudioLoadResultHandler(PlayerManagerService playerManagerService, EmbedBuilder embedBuilder, SlashCommandInteractionEvent event, GuildMusicManager musicManager, MusicPojo musicPojo) {
        this.playerManagerService = playerManagerService;
        this.embedBuilder = embedBuilder;
        this.event = event;
        this.musicManager = musicManager;
        this.musicPojo = musicPojo;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        embedBuilder.clear();
        event.replyEmbeds(
            embedBuilder
                .setDescription(
                        "Song added to queue: " + track.getInfo().title
                                + "\n in queue: " + (musicManager.scheduler.queue.size() + 1))
                .setColor(Color.GREEN)
                .build()
        ).queue();

        musicManager.scheduler.setEvent(event);
        musicManager.scheduler.queue(track);
        String title = musicPojo.getTitle();
        playerManagerService.cacheTrack(track, title);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        List<AudioTrack> tracks = playlist.getTracks();

        for (AudioTrack track : tracks) {
            musicManager.scheduler.queue(track);
        }
        event.replyEmbeds(
                embedBuilder
                        .setDescription(tracks.size() + " song added to queue.")
                        .setColor(Color.GREEN)
                        .build()
        ).queue();
    }

    @Override
    public void noMatches() {
    }

    @Override
    public void loadFailed(FriendlyException exception) {
    }
}
