package com.discord.bot.commands.musiccommands;

import com.discord.bot.audioplayer.GuildMusicManager;
import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.entity.pojo.MusicDto;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.RestService;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class PlayCommand implements ISlashCommand {
    RestService restService;
    PlayerManagerService playerManagerService;
    MusicCommandUtils utils;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var option = event.getOption("query");
        var ephemeralOption = event.getOption("ephemeral");
        boolean ephemeral = ephemeralOption == null || ephemeralOption.getAsBoolean();

        if (option != null) {
            String query = option.getAsString().trim();
            List<MusicDto> musicDtoList = new ArrayList<>(getYoutubeLink(query, event));
            playMusic(event, musicDtoList, ephemeral);
        } else {
            event.replyEmbeds(new EmbedBuilder()
                            .setDescription("Option query can't be null.")
                            .setColor(Color.RED)
                            .build())
                    .setEphemeral(ephemeral)
                    .queue();
        }
    }

    private void playMusic(SlashCommandInteractionEvent event, List<MusicDto> musicDtos, boolean ephemeral) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        AudioChannel userChannel = getAudioChannel(event, false);
        AudioChannel botChannel = getAudioChannel(event, true);

        if (userChannel != null) {
            int trackSize = musicDtos.size();
            if (trackSize != 0) {
                if (botChannel == null) {
                    GuildMusicManager musicManager = playerManagerService.getMusicManager(event);
                    utils.playerCleaner(musicManager);

                    if (!userChannel.getGuild().getSelfMember().hasPermission(userChannel, Permission.VOICE_CONNECT)) {
                        event.replyEmbeds(new EmbedBuilder()
                                        .setDescription("Bot does not have permission to join the voice channel.")
                                        .setColor(Color.RED)
                                        .build())
                                .setEphemeral(ephemeral)
                                .queue();
                        return;
                    }
                    userChannel.getGuild().getAudioManager().openAudioConnection(userChannel);
                    botChannel = userChannel;
                }
                if (botChannel.equals(userChannel)) {
                    if (trackSize == 1) {
                        if (musicDtos.get(0).getYoutubeUri() == null) {
                            musicDtos.set(0, restService.getYoutubeLink(musicDtos.get(0)));
                        }
                        playerManagerService.loadAndPlay(event, musicDtos.get(0), ephemeral);
                    } else playerManagerService.loadMultipleAndPlay(event, musicDtos, ephemeral);
                } else
                    embedBuilder.setDescription("Please be in the same voice channel as the bot.").setColor(Color.RED);
            } else embedBuilder.setDescription("No tracks found.").setColor(Color.RED);
        } else embedBuilder.setDescription("Please join a voice channel.").setColor(Color.RED);

        event.replyEmbeds(embedBuilder.build()).setEphemeral(ephemeral).queue();
    }

    private AudioChannel getAudioChannel(SlashCommandInteractionEvent event, boolean self) {
        AudioChannelUnion audioChannel = null;

        var member = event.getMember();
        if (member != null) {
            if (self) {
                var guild = member.getGuild();
                member = guild.getSelfMember();
            }
            var voiceState = member.getVoiceState();
            if (voiceState != null) {
                audioChannel = voiceState.getChannel();
            }
        }

        return audioChannel;
    }

    private List<MusicDto> getYoutubeLink(String query, GenericCommandInteractionEvent event) {
        List<MusicDto> musicDtos = new ArrayList<>();
        MusicDto musicDto;
        if (query.contains("https://www.youtube.com/watch?v=")
                || query.contains("https://youtu.be/")
                || query.contains("https://youtube.com/playlist?list=")
                || query.contains("https://music.youtube.com/watch?v=")
                || query.contains("https://music.youtube.com/playlist?list=")
                || query.contains("https://www.twitch.tv/")
                || query.contains("https://soundcloud.com/")
        ) {
            musicDto = new MusicDto(null, query);
            musicDtos.add(musicDto);
        } else if (query.contains("https://open.spotify.com/")) {
            musicDtos = spotifyToYoutube(query);
        } else {
            musicDto = restService.getYoutubeLink(new MusicDto(query, null));
            if (musicDto.getYoutubeUri().equals("403glaxierror")) {
                apiLimitExceeded(event.getMessageChannel());
            } else {
                musicDtos.add(musicDto);
            }
        }
        return musicDtos;
    }

    private List<MusicDto> spotifyToYoutube(String spotifyUrl) {
        return new ArrayList<>(restService.getSpotifyMusicName(spotifyUrl));
    }

    private void apiLimitExceeded(MessageChannel channel) {
        channel.sendMessageEmbeds(new EmbedBuilder()
                        .setDescription("Youtube quota has exceeded. " +
                                "Please use youtube links to play music for today.")
                        .build())
                .queue();
    }
}
