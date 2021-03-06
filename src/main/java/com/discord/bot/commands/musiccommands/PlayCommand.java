package com.discord.bot.commands.musiccommands;

import com.discord.bot.audioplayer.GuildMusicManager;
import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.entity.pojo.MusicPojo;
import com.discord.bot.service.RestService;
import com.discord.bot.service.TrackService;
import com.discord.bot.service.audioplayer.PlayerManagerService;
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
        List<MusicPojo> musicPojos = new ArrayList<>(getYoutubeLink(query, event));
        playMusic(event, musicPojos);
    }

    private void playMusic(SlashCommandInteractionEvent event, List<MusicPojo> musicPojos) {
        AudioChannel userChannel = event.getMember().getVoiceState().getChannel();
        AudioChannel botChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();
        boolean isUserInVoiceChannel = event.getMember().getVoiceState().inAudioChannel();
        boolean isBotInVoiceChannel = event.getGuild().getSelfMember().getVoiceState().inAudioChannel();

        if (isUserInVoiceChannel) {
            int trackSize = musicPojos.size();
            if (trackSize != 0) {
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
                    if (trackSize == 1) {
                        if (musicPojos.get(0).getYoutubeUri() == null) {
                            musicPojos.set(0, restService.getYoutubeLink(musicPojos.get(0)));
                        }
                        playerManagerService.loadAndPlay(event, musicPojos.get(0));
                    } else {
                        playerManagerService.loadMultipleAndPlay(event, musicPojos);
                    }
                } else {
                    event.replyEmbeds(new EmbedBuilder().setDescription("Please be in same channel with bot.")
                            .build()).queue();
                }
            } else {
                event.replyEmbeds(new EmbedBuilder().setDescription("No tracks found.")
                        .setColor(Color.RED).build()).queue();
            }
        } else {
            event.replyEmbeds(new EmbedBuilder().setDescription("Please join to a voice channel.").build()).queue();
        }
    }

    private List<MusicPojo> getYoutubeLink(String query, SlashCommandInteractionEvent event) {
        List<MusicPojo> musicPojos = new ArrayList<>();
        MusicPojo musicPojo;
        if (query.contains("https://www.youtube.com/watch?v=")) {
            musicPojo = new MusicPojo(null, query);
            musicPojos.add(musicPojo);
        } else if (query.contains("https://open.spotify.com/")) {
            musicPojos = spotifyToYoutube(query);
        } else {
            musicPojo = restService.getYoutubeLink(new MusicPojo(query, null));
            if (musicPojo.getYoutubeUri().equals("403glaxierror")) {
                apiLimitExceeded(event.getChannel());
            } else {
                musicPojos.add(musicPojo);
            }
        }
        return musicPojos;
    }

    private List<MusicPojo> spotifyToYoutube(String spotifyUrl) {
        List<MusicPojo> musicPojoNameList = new ArrayList<>(restService.getSpotifyMusicName(spotifyUrl));
        return musicPojoNameList;
    }

    private void apiLimitExceeded(MessageChannel channel) {
        channel.sendMessageEmbeds(new EmbedBuilder().setDescription("Youtube quota has exceeded. " +
                "Please use youtube links to play music for today.").build()).queue();
    }
}
