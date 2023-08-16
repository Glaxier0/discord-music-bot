package com.discord.bot.audioplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class GuildMusicManager {
    public final AudioPlayer audioPlayer;
    private final AudioPlayerSendHandler sendHandler;
    public TrackScheduler scheduler;

    public GuildMusicManager(AudioPlayerManager manager, SlashCommandInteractionEvent event) {
        this.audioPlayer = manager.createPlayer();
        this.scheduler = new TrackScheduler(this.audioPlayer, event);
        this.audioPlayer.addListener(this.scheduler);
        this.sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
    }

    public GuildMusicManager(AudioPlayerManager manager, ButtonInteractionEvent event) {
        this.audioPlayer = manager.createPlayer();
        this.scheduler = new TrackScheduler(this.audioPlayer, event);
        this.audioPlayer.addListener(this.scheduler);
        this.sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
    }

    public AudioPlayerSendHandler getSendHandler() {
        return sendHandler;
    }
}
