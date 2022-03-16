package com.discord.bot.audioplayer;

import com.discord.bot.service.TrackService;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class GuildMusicManager {
    public final AudioPlayer audioPlayer;
    private final AudioPlayerSendHandler sendHandler;
    public TrackScheduler scheduler;
    TrackService trackService;

    public GuildMusicManager(AudioPlayerManager manager, SlashCommandInteractionEvent event, TrackService trackService) {
        this.audioPlayer = manager.createPlayer();
        this.scheduler = new TrackScheduler(this.audioPlayer, event, trackService);
        this.audioPlayer.addListener(this.scheduler);
        this.sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
    }

    public AudioPlayerSendHandler getSendHandler() {
        return sendHandler;
    }
}
