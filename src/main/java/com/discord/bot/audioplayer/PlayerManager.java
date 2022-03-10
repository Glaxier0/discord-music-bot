package com.discord.bot.audioplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerManager {
    private static PlayerManager INSTANCE;

    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;

    public PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
    }

    public static PlayerManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }

        return INSTANCE;
    }

    public GuildMusicManager getMusicManager(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager
                    , guild.getAudioManager(), event);

            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());

            return guildMusicManager;
        });
    }

    public void loadAndPlay(SlashCommandInteractionEvent event, String trackUrl) {
        final GuildMusicManager musicManager = this.getMusicManager(event);

        this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.scheduler.queue(track);
                int musicCount = musicManager.scheduler.queue.size();
                if (musicCount > 0) {
                    event.replyEmbeds(new EmbedBuilder().setDescription("Song added to queue: " + track.getInfo().title
                            + "\n in queue: " + (musicCount)).setColor(Color.GREEN).build()).queue();
                } else {
                    event.replyEmbeds(new EmbedBuilder().setTitle("Song added to queue "
                            + track.getInfo().title).setColor(Color.GREEN).build()).queue();
                }
                musicManager.scheduler.setEvent(event);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                List<AudioTrack> tracks = playlist.getTracks();

                for (AudioTrack track : tracks) {
                    musicManager.scheduler.queue(track);
                }
                event.replyEmbeds(new EmbedBuilder().setDescription(tracks.size() + " song added to queue.")
                        .setColor(Color.GREEN).build()).queue();
            }

            @Override
            public void noMatches() {
                //
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                //
            }
        });
    }

    public void loadMultipleAndPlay(SlashCommandInteractionEvent event, List<String> trackUrls) {
        final GuildMusicManager musicManager = this.getMusicManager(event);
        for (String trackUrl : trackUrls) {
            this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    musicManager.scheduler.queue(track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    //
                }

                @Override
                public void noMatches() {
                    //
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    //
                }
            });
        }
        event.replyEmbeds(new EmbedBuilder().setDescription(trackUrls.size() + " tracks queued.").build()).queue();
    }

}