package com.discord.bot.audioplayer.AudioLoadResultHandler;

import com.discord.bot.audioplayer.GuildMusicManager;
import com.discord.bot.entity.pojo.MusicPojo;
import com.discord.bot.service.TrackService;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class MultipleAudioLoadResultHandler implements AudioLoadResultHandler {
    private final GuildMusicManager musicManager;
    private final MusicPojo musicPojo;
    
    TrackService trackService;

    public MultipleAudioLoadResultHandler(GuildMusicManager musicManager, MusicPojo musicPojo, TrackService trackService) {
        this.musicManager = musicManager;
        this.musicPojo = musicPojo;
        this.trackService = trackService;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        musicManager.scheduler.queue(track);
        String title = musicPojo.getTitle();
        trackService.cache(title, track.getInfo().uri);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
    }

    @Override
    public void noMatches() {
    }

    @Override
    public void loadFailed(FriendlyException exception) {
    }
}
