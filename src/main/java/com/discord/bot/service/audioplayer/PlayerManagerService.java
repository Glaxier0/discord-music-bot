package com.discord.bot.service.audioplayer;

import com.discord.bot.audioplayer.GuildMusicManager;
import com.discord.bot.entity.pojo.MusicPojo;
import com.discord.bot.entity.MusicData;
import com.discord.bot.service.RestService;
import com.discord.bot.service.TrackService;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlayerManagerService {
    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;
    TrackService trackService;
    RestService restService;

    public PlayerManagerService(TrackService trackService, RestService restService) {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
        this.trackService = trackService;
        this.restService = restService;
    }

    public GuildMusicManager getMusicManager(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager, event);

            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());

            return guildMusicManager;
        });
    }

    public void loadAndPlay(SlashCommandInteractionEvent event, MusicPojo musicPojo) {
        final GuildMusicManager musicManager = this.getMusicManager(event);
        EmbedBuilder embedBuilder = new EmbedBuilder();
        this.audioPlayerManager.loadItemOrdered(musicManager, musicPojo.getYoutubeUri(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                embedBuilder.clear();
                event.replyEmbeds(embedBuilder.setDescription("Song added to queue: " + track.getInfo().title
                        + "\n in queue: " + (musicManager.scheduler.queue.size() + 1)).setColor(Color.GREEN).build()).queue();
                musicManager.scheduler.setEvent(event);
                musicManager.scheduler.queue(track);
                String title = musicPojo.getTitle();
                cacheTrack(track, title);
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
                System.out.println("No match found");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                exception.printStackTrace();
            }
        });
    }
    @Async
    public void loadMultipleAndPlay(SlashCommandInteractionEvent event, List<MusicPojo> musicPojos) {
        final GuildMusicManager musicManager = this.getMusicManager(event);
        musicManager.scheduler.setEvent(event);
        EmbedBuilder embedBuilder = new EmbedBuilder();
        event.replyEmbeds(embedBuilder.setDescription("Reading spotify playlist.")
                .setColor(Color.GREEN).build()).queue();
        int errorCounter = 0;
        for (MusicPojo musicPojo : musicPojos) {
            musicPojo.setYoutubeUri(restService.getYoutubeLink(musicPojo).getYoutubeUri());
            if (musicPojo.getYoutubeUri().equals("403glaxierror")) {
                errorCounter++;
                continue;
            }
            this.audioPlayerManager.loadItemOrdered(musicManager, musicPojo.getYoutubeUri(), new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    musicManager.scheduler.queue(track);
                    String title = musicPojo.getTitle();
                    cacheTrack(track, title);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    //
                }

                @Override
                public void noMatches() {
                    System.out.println("No match found");
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    exception.printStackTrace();
                }
            });
        }
        if (errorCounter > 0) {
            apiLimitExceeded(event.getChannel());
        }
        event.getChannel().sendMessageEmbeds(new EmbedBuilder().setDescription(musicPojos.size() - errorCounter + " tracks queued.")
                .build()).queue();
    }

    private void cacheTrack(AudioTrack track, String title) {
        if (title != null) {
            MusicData musicData = new MusicData(title, track.getInfo().uri);
            MusicData redisMusicData = trackService.findFirst1ByTitle(musicData.getTitle());
            if (redisMusicData == null) {
                trackService.save(musicData);
            }
        }
    }

    private void apiLimitExceeded(MessageChannel channel) {
        channel.sendMessageEmbeds(new EmbedBuilder().setDescription("Youtube quota has exceeded. " +
                "Please use youtube links to play music for today.").build()).queue();
    }
}