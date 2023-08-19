package com.discord.bot.commands.musiccommands;

import com.discord.bot.audioplayer.GuildMusicManager;
import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.entity.pojo.MusicPojo;
import com.discord.bot.service.RestService;
import com.discord.bot.service.TrackService;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        var option = event.getOption("query");

        if (option != null) {
            String query = option.getAsString().trim();
            List<MusicPojo> musicPojos = new ArrayList<>(getYoutubeLink(query, event));
            playMusic(event, musicPojos);
        } else {
            event.replyEmbeds(new EmbedBuilder().setDescription("Option query can't be null.")
                    .setColor(Color.RED).build()).queue();
        }
    }

    private void playMusic(SlashCommandInteractionEvent event, List<MusicPojo> musicPojos) {
        AudioChannel userChannel = Objects.requireNonNull(Objects
                .requireNonNull(event.getMember()).getVoiceState()).getChannel();
        AudioChannel botChannel = Objects.requireNonNull(Objects.
                requireNonNull(event.getGuild()).getSelfMember().getVoiceState()).getChannel();
        boolean isUserInVoiceChannel = event.getMember().getVoiceState().inAudioChannel();
        boolean isBotInVoiceChannel = event.getGuild().getSelfMember().getVoiceState().inAudioChannel();

        if (isUserInVoiceChannel) {
            int trackSize = musicPojos.size();
            if (trackSize != 0) {
                if (!isBotInVoiceChannel) {
                    GuildMusicManager musicManager = playerManagerService.getMusicManager(event);
                    musicManager.scheduler.repeating = false;
                    musicManager.scheduler.player.setPaused(false);
                    musicManager.scheduler.player.stopTrack();
                    musicManager.scheduler.queue.clear();
                    try {
                        event.getGuild().getAudioManager().openAudioConnection(userChannel);
                    } catch (InsufficientPermissionException exception) {
                        event.replyEmbeds(new EmbedBuilder().setDescription("Bot does not have permission to join the voice channel.")
                                .setColor(Color.RED).build()).queue();
                        return;
                    }
                    botChannel = userChannel;
                }
                if (botChannel != null && botChannel.equals(userChannel)) {
                    if (trackSize == 1) {
                        if (musicPojos.get(0).getYoutubeUri() == null) {
                            musicPojos.set(0, restService.getYoutubeLink(musicPojos.get(0)));
                        }
                        playerManagerService.loadAndPlay(event, musicPojos.get(0));
                    } else {
                        playerManagerService.loadMultipleAndPlay(event, musicPojos);
                    }
                } else {
                    event.replyEmbeds(new EmbedBuilder().setDescription("Please be in the same voice channel as the bot.")
                            .setColor(Color.RED).build()).queue();
                }
            } else {
                event.replyEmbeds(new EmbedBuilder().setDescription("No tracks found.")
                        .setColor(Color.RED).build()).queue();
            }
        } else {
            event.replyEmbeds(new EmbedBuilder().setDescription("Please join a voice channel.")
                    .setColor(Color.RED).build()).queue();
        }
    }

    private List<MusicPojo> getYoutubeLink(String query, GenericCommandInteractionEvent event) {
        List<MusicPojo> musicPojos = new ArrayList<>();
        MusicPojo musicPojo;
        if (query.contains("https://www.youtube.com/watch?v=")
                || query.contains("https://youtu.be/")
                || query.contains("https://youtube.com/playlist?list=")
                || query.contains("https://music.youtube.com/watch?v=")
                || query.contains("https://music.youtube.com/playlist?list=")
                || query.contains("https://www.twitch.tv/")
                || query.contains("https://soundcloud.com/")
        ) {
            musicPojo = new MusicPojo(null, query);
            musicPojos.add(musicPojo);
        } else if (query.contains("https://open.spotify.com/")) {
            musicPojos = spotifyToYoutube(query);
        } else {
            musicPojo = restService.getYoutubeLink(new MusicPojo(query, null));
            if (musicPojo.getYoutubeUri().equals("403glaxierror")) {
                apiLimitExceeded(event.getMessageChannel());
            } else {
                musicPojos.add(musicPojo);
            }
        }
        return musicPojos;
    }

    private List<MusicPojo> spotifyToYoutube(String spotifyUrl) {
        return new ArrayList<>(restService.getSpotifyMusicName(spotifyUrl));
    }

    private void apiLimitExceeded(MessageChannel channel) {
        channel.sendMessageEmbeds(new EmbedBuilder().setDescription("Youtube quota has exceeded. " +
                "Please use youtube links to play music for today.").build()).queue();
    }
}
