package com.discord.bot.audioplayer.AudioLoadResultHandler;

import com.discord.bot.audioplayer.GuildMusicManager;
import com.discord.bot.entity.pojo.MusicPojo;
import com.discord.bot.service.TrackService;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.discord.bot.utils.EmbedMessageSender;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.List;

public class SingleAudioLoadResultHandler implements AudioLoadResultHandler {
    private final SlashCommandInteractionEvent event;
    private final GuildMusicManager musicManager;
    private final MusicPojo musicPojo;
    TrackService trackService;

    public SingleAudioLoadResultHandler(GuildMusicManager musicManager,SlashCommandInteractionEvent event,  MusicPojo musicPojo, TrackService trackService) {
        this.event = event;
        this.musicManager = musicManager;
        this.musicPojo = musicPojo;
        this.trackService = trackService;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.clear();
        EmbedMessageSender.sendReplyEmbed(
            event,
            embedBuilder
                .setDescription(
                    "Song added to queue: " + track.getInfo().title
                            + "\n in queue: " + (musicManager.scheduler.queue.size() + 1))
                .setColor(Color.GREEN)
                .build()
        );

        musicManager.scheduler.setEvent(event);
        musicManager.scheduler.queue(track);
        String title = musicPojo.getTitle();
        trackService.cache(title, track.getInfo().uri);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        List<AudioTrack> tracks = playlist.getTracks();

        for (AudioTrack track : tracks) {
            musicManager.scheduler.queue(track);
        }
        EmbedMessageSender.sendReplyEmbed(
            event,
            new EmbedBuilder()
                .setDescription(tracks.size() + " song added to queue.")
                .setColor(Color.GREEN)
                .build()
        );
    }

    @Override
    public void noMatches() {
    }

    @Override
    public void loadFailed(FriendlyException exception) {
    }
}
