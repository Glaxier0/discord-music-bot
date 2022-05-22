package com.discord.bot.audioplayer.AudioLoadResultHandler;

import com.discord.bot.audioplayer.GuildMusicManager;
import com.discord.bot.entity.pojo.MusicPojo;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class MultipleAudioLoadResultHandler implements AudioLoadResultHandler {
    private final PlayerManagerService playerManagerService;
    private final GuildMusicManager musicManager;
    private final MusicPojo musicPojo;

    public MultipleAudioLoadResultHandler(PlayerManagerService playerManagerService, GuildMusicManager musicManager, MusicPojo musicPojo) {
        this.playerManagerService = playerManagerService;
        this.musicManager = musicManager;
        this.musicPojo = musicPojo;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        musicManager.scheduler.queue(track);
        String title = musicPojo.getTitle();
        playerManagerService.cacheTrack(track, title);
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
