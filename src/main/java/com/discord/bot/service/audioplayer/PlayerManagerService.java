package com.discord.bot.service.audioplayer;

import com.discord.bot.audioplayer.GuildMusicManager;
import com.discord.bot.entity.pojo.MusicPojo;
import com.discord.bot.entity.MusicData;
import com.discord.bot.loader.MusicLoaderManager;
import com.discord.bot.service.RestService;
import com.discord.bot.service.TrackService;
import com.discord.bot.service.audioplayer.AudioLoadResultHandler.MultipleAudioLoadResultHandler;
import com.discord.bot.service.audioplayer.AudioLoadResultHandler.SingleAudioLoadResultHandler;
import com.discord.bot.utils.EmbedMessageSender;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;

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

    private GuildMusicManager getMusicManager(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        return this.musicManagers.computeIfAbsent(
            guild.getIdLong(),
            (guildId) -> {
                final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager, event);
    
                guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
    
                return guildMusicManager;
            }
        );
    }

    public void playQueriedTrack(SlashCommandInteractionEvent event) {
        String query = event.getOption("query").getAsString().trim();

        List<MusicPojo> musicPojos = new MusicLoaderManager().loadMusicUsingQuery(restService, query, event);
        if (musicPojos.size() == 1) {
            loadAndPlay(event, musicPojos.get(0));
        } else {
            loadMultipleAndPlay(event, musicPojos);
        }
    }
    
    private void loadAndPlay(SlashCommandInteractionEvent event, MusicPojo musicPojo) {
        final GuildMusicManager musicManager = this.getMusicManager(event);
        AudioLoadResultHandler resultHandler = new SingleAudioLoadResultHandler(musicManager, event, musicPojo, trackService);
        this.audioPlayerManager.loadItemOrdered(
                musicManager,
                musicPojo.getYoutubeUri(),
                resultHandler
        );
    }
    
    @Async
    private void loadMultipleAndPlay(SlashCommandInteractionEvent event, List<MusicPojo> musicPojos) {
        final GuildMusicManager musicManager = this.getMusicManager(event);
        musicManager.scheduler.setEvent(event);

        EmbedMessageSender.sendReplyEmbed(
            event,
            new EmbedBuilder()
                .setDescription("Reading spotify playlist.")
                .setColor(Color.GREEN)
                .build()
        );

        int errorCounter = 0;
        for (MusicPojo musicPojo : musicPojos) {
            musicPojo.setYoutubeUri(restService.getYoutubeLink(musicPojo).getYoutubeUri());
            if (musicPojo.getYoutubeUri().equals("403glaxierror")) {
                errorCounter++;
                continue;
            }
            
            this.audioPlayerManager.loadItemOrdered(
                musicManager,
                musicPojo.getYoutubeUri(),
                new MultipleAudioLoadResultHandler(musicManager, musicPojo, trackService)
            );
        }
        if (errorCounter > 0) {
            EmbedMessageSender.sendEmbedToChannel(
                    event.getChannel(),
                new EmbedBuilder()
                    .setDescription("Youtube quota has exceeded. " + "Please use youtube links to play music for today.")
                    .build()
            );
        }
        EmbedMessageSender.sendEmbedToChannel(
            event.getChannel(),
            new EmbedBuilder()
                .setDescription(musicPojos.size() - errorCounter + " tracks queued.")
                .build()
        );
    }

   

    public void pauseTrack(SlashCommandInteractionEvent event, EmbedBuilder embedBuilder) {
        getMusicManager(event).audioPlayer.setPaused(true);
        event.replyEmbeds(embedBuilder.setDescription("Song paused")
                .setColor(Color.GREEN).build()).queue();
    }

    public void resumeTrack(SlashCommandInteractionEvent event, EmbedBuilder embedBuilder) {
        getMusicManager(event).audioPlayer.setPaused(false);
        event.replyEmbeds(embedBuilder.setDescription("Song resumed")
                .setColor(Color.GREEN).build()).queue();
    }

    public void skipToNextTrack(SlashCommandInteractionEvent event, EmbedBuilder embedBuilder) {
        getMusicManager(event).scheduler.nextTrack();
        event.replyEmbeds(embedBuilder.setDescription("Song skipped").setColor(Color.GREEN).build()).queue();
    }

    public void showTrackQueue(SlashCommandInteractionEvent event, EmbedBuilder embedBuilder) {
        BlockingQueue<AudioTrack> queue = this.getMusicManager(event).scheduler.queue;
        int trackCount = Math.min(queue.size(), 20);
        List<AudioTrack> trackList = new ArrayList<>(queue);

        embedBuilder.setTitle("Current Queue:");
        for (int i = 0; i < trackCount; i++) {
            AudioTrack track = trackList.get(i);
            AudioTrackInfo info = track.getInfo();
            embedBuilder.appendDescription((i + 1) + ". " + info.title + "\n");
        }

        if (trackList.size() > trackCount) {
            embedBuilder.appendDescription("And " + (trackList.size() - trackCount) + " more...");
        }

        event.replyEmbeds(embedBuilder.build()).queue();
    }

    public boolean isQueueNotEmpty(SlashCommandInteractionEvent event) {
        BlockingQueue<AudioTrack> queue = getMusicManager(event).scheduler.queue;
        return !queue.isEmpty();
    }

    public void toggleLoopCurrentTrack(SlashCommandInteractionEvent event, EmbedBuilder embedBuilder) {
        boolean repeat = getMusicManager(event).scheduler.repeating;
        getMusicManager(event).scheduler.repeating = !repeat;
        if (!repeat) {
            event.replyEmbeds(embedBuilder.setDescription(":white_check_mark: Track loop enabled.").setColor(Color.GREEN).build()).queue();
        } else {
            event.replyEmbeds(embedBuilder.setDescription(":x: Track loop disabled.").setColor(Color.RED).build()).queue();
        }
    }

    public void swapTwoTrackInQueue(SlashCommandInteractionEvent event, EmbedBuilder embedBuilder) {
        GuildMusicManager musicManager = getMusicManager(event);
        List<AudioTrack> trackList = new ArrayList<>(musicManager.scheduler.queue);

        if (trackList.size() > 1) {
            swapTrack(event, musicManager, trackList);
        } else if (trackList.size() == 1) {
            event.replyEmbeds(embedBuilder.setDescription("There is only one song in queue.")
                    .setColor(Color.RED).build()).queue();
        } else {
            event.replyEmbeds(embedBuilder.setDescription("Queue is empty.")
                    .setColor(Color.RED).build()).queue();
        }
    }
    private void swapTrack(SlashCommandInteractionEvent event, GuildMusicManager musicManager, List<AudioTrack> trackList) {
        int first = event.getOption("songnum1").getAsInt() - 1;
        int second = event.getOption("songnum2").getAsInt() - 1;

        try {
            AudioTrack temp = trackList.get(first);
            trackList.set(first, trackList.get(second));
            trackList.set(second, temp);
        } catch (Exception e) {
            event.replyEmbeds(new EmbedBuilder()
                    .setDescription("Please enter a valid queue ids for both of the songs.")
                    .setColor(Color.RED).build()).queue();
            return;
        }

        musicManager.scheduler.queue.clear();
        for (AudioTrack track : trackList) {
            musicManager.scheduler.queue(track);
        }

        event.replyEmbeds(new EmbedBuilder()
                .setDescription("Successfully swapped order of two songs")
                .setColor(Color.GREEN).build()).queue();
    }

    public void shuffleCurrentTrackQueue(SlashCommandInteractionEvent event, EmbedBuilder embedBuilder) {
        GuildMusicManager musicManager = getMusicManager(event);
        List<AudioTrack> trackList = new ArrayList<>(musicManager.scheduler.queue);
        if (trackList.size() > 1) {
            ShuffleCollection(musicManager, trackList);
            embedBuilder.setDescription("Queue shuffled").setColor(Color.GREEN);
        } else {
            embedBuilder.setDescription("Queue size have to be at least two.").setColor(Color.RED);
        }
        event.replyEmbeds(embedBuilder.build()).queue();
    }

    private void ShuffleCollection(GuildMusicManager musicManager, List<AudioTrack> trackList) {
        Collections.shuffle(trackList);
        musicManager.scheduler.queue.clear();

        for (AudioTrack track : trackList) {
            musicManager.scheduler.queue(track);
        }
    }

    public void cleanUpCurrentTrackSchedule(SlashCommandInteractionEvent event) {
        GuildMusicManager musicManager = getMusicManager(event);
        if (musicManager.scheduler.repeating) {
            musicManager.scheduler.repeating = false;
        }
        musicManager.scheduler.queue.clear();
    }
    
    public void leavePlayer(SlashCommandInteractionEvent event, EmbedBuilder embedBuilder) {
        GuildMusicManager musicManager = this.getMusicManager(event);
        AudioManager audioManager = event.getGuild().getAudioManager();
        musicManager.scheduler.player.stopTrack();
        musicManager.scheduler.queue.clear();
        audioManager.closeAudioConnection();
        event.replyEmbeds(embedBuilder.setDescription("Bye.").build()).queue();
    }

}
