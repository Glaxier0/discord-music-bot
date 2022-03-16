package com.discord.bot.commands.musiccommands;

import com.discord.bot.audioplayer.GuildMusicManager;
import com.discord.bot.audioplayer.PlayerManagerService;
import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.entity.MusicData;
import com.discord.bot.service.RestService;
import com.discord.bot.service.TrackService;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PlayCommand implements ISlashCommand {
    RestService restService;
    PlayerManagerService playerManagerService;
    TrackService trackService;

    public PlayCommand(RestService restService, PlayerManagerService playerManagerService, TrackService trackService) {
        this.restService = restService;
        this.playerManagerService = playerManagerService;
        this.trackService = trackService;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String query = event.getOption("query").getAsString().trim();
        try {
            String youtubeUri = getFromCache(query);
            System.out.println(youtubeUri);
            event.getGuild().getAudioManager().openAudioConnection(event.getMember().getVoiceState().getChannel());
            playerManagerService.loadAndPlay(event, youtubeUri);
        } catch (Exception e) {
            List<String> youtubeLinks = getYoutubeLink(query, event);
            playMusic(event, youtubeLinks);
        }
    }

    private void playMusic(SlashCommandInteractionEvent event, List<String> youtubeLinks) {
        AudioChannel userChannel = event.getMember().getVoiceState().getChannel();
        AudioChannel botChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();
        boolean isUserInVoiceChannel = event.getMember().getVoiceState().inAudioChannel();
        boolean isBotInVoiceChannel = event.getGuild().getSelfMember().getVoiceState().inAudioChannel();

        if (isUserInVoiceChannel) {
            if (!youtubeLinks.isEmpty()) {
                if (!isBotInVoiceChannel) {
                    GuildMusicManager musicManager = playerManagerService.getMusicManager(event);
                    if (musicManager.scheduler.repeating) {
                        musicManager.scheduler.repeating = false;
                    }
                    musicManager.scheduler.queue.clear();
                    event.getGuild().getAudioManager().openAudioConnection(userChannel);
                    botChannel = userChannel;
                }
                if (botChannel.equals(userChannel)) {
                    int trackSize = youtubeLinks.size();
                    if (trackSize == 1) {
                        playerManagerService.loadAndPlay(event, youtubeLinks.get(0));
                    } else if (trackSize > 1) {
                        playerManagerService.loadMultipleAndPlay(event, youtubeLinks);
                    }
                } else {
                    event.replyEmbeds(new EmbedBuilder().setDescription("Please be in same channel with bot.")
                            .build()).queue();
                }
            }
        } else {
            event.replyEmbeds(new EmbedBuilder().setDescription("Please join to a voice channel.").build()).queue();
        }
    }

    private ArrayList<String> getYoutubeLink(String query, SlashCommandInteractionEvent event) {
        ArrayList<String> youtubeLinks = new ArrayList<>();
        if (query.contains("https://www.youtube.com/watch?v=")) {
            youtubeLinks.add(query);
        } else if (query.contains("https://open.spotify.com/")) {
            event.replyEmbeds(new EmbedBuilder().setDescription("Spotify links are not supported.")
                    .setColor(Color.RED).build()).queue();
//            youtubeLinks = spotifyToYoutube(query);
//
//            if (youtubeLinks.get(youtubeLinks.size() - 1).equals("403glaxierror")) {
//                youtubeLinks.remove(youtubeLinks.size() - 1);
//                apiLimitExceeded(channel);
//            }
        } else {
            String youtubeLink = restService.getYoutubeLink(query);
            if (youtubeLink.equals("403glaxierror")) {
                apiLimitExceeded(event.getChannel());
            } else {
                youtubeLinks.add(youtubeLink);
            }
        }
        return youtubeLinks;
    }

    private void apiLimitExceeded(MessageChannel channel) {
        channel.sendMessageEmbeds(new EmbedBuilder().setDescription("Youtube quota has exceeded. " +
                "Please use youtube links to play music for today.").build()).queue();
    }

    private String getFromCache(String title) {
        System.out.println(title);
        long start = System.currentTimeMillis();
        MusicData musicData = trackService.findFirst1ByTitle(title);
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.println("Cache: " + timeElapsed);
        return musicData.getYoutubeUri();
//        String youtubeId = musicData.getYoutubeUri().substring(32);
//        YoutubeAudioSourceManager youtubeAudioSourceManager = new YoutubeAudioSourceManager();
//        AudioItem audioItem = youtubeAudioSourceManager.loadTrackWithVideoId(youtubeId, true);
//        AudioTrack audioTrack = (AudioTrack) audioItem;
//        GuildMusicManager musicManager = playerManagerService.getMusicManager(event);
//        musicManager.scheduler.queue(audioTrack);
    }
}
