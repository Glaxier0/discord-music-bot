package com.discord.bot.commands.musiccommands;

import com.discord.bot.audioplayer.GuildMusicManager;
import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.dto.MultipleMusicDto;
import com.discord.bot.dto.MusicDto;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.RestService;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import org.apache.coyote.BadRequestException;

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
        boolean ephemeral = shouldReplyEphemeral(event);
        event.deferReply(ephemeral).queue();

        var queryOption = event.getOption("query");
        var fileOption = event.getOption("file");

        if (isInvalidInputCombination(queryOption, fileOption, event, ephemeral))
            return;

        try {
            MultipleMusicDto multipleMusicDto = processInput(queryOption, fileOption);
            if (multipleMusicDto.getCount() == 0) {
                sendErrorMessage(event, "YouTube quota has exceeded. Please use YouTube URLs to play music for today.", ephemeral);
                return;
            }
            playMusic(event, multipleMusicDto, ephemeral);
        } catch (BadRequestException e) {
            sendErrorMessage(event, "The maximum allowed Spotify playlist size is 50.", ephemeral);
        }
    }

    private void playMusic(SlashCommandInteractionEvent event, MultipleMusicDto multipleMusicDto, boolean ephemeral) {
        AudioChannel userChannel = getAudioChannel(event, false);
        if (userChannel == null) {
            sendErrorMessage(event, "Please join a voice channel.", ephemeral);
            return;
        }

        AudioChannel botChannel = getAudioChannel(event, true);
        if (!canPlayInChannel(userChannel, botChannel, event, ephemeral)) return;

        loadAndPlayTracks(event, multipleMusicDto, userChannel, botChannel, ephemeral);
    }

    private AudioChannel getAudioChannel(SlashCommandInteractionEvent event, boolean self) {
        AudioChannelUnion audioChannel = null;

        var member = event.getMember();
        if (member != null) {
            if (self) {
                member = member.getGuild().getSelfMember();
            }
            var voiceState = member.getVoiceState();
            if (voiceState != null) {
                audioChannel = voiceState.getChannel();
            }
        }

        return audioChannel;
    }

    private boolean canPlayInChannel(AudioChannel userChannel, AudioChannel botChannel,
            SlashCommandInteractionEvent event, boolean ephemeral) {
        if (botChannel == null) {
            if (!userChannel.getGuild().getSelfMember().hasPermission(userChannel, Permission.VOICE_CONNECT)) {
                sendErrorMessage(event, "Bot does not have permission to join the voice channel.", ephemeral);
                return false;
            }
            userChannel.getGuild().getAudioManager().openAudioConnection(userChannel);
        } else if (!botChannel.equals(userChannel)) {
            sendErrorMessage(event, "Please be in the same voice channel as the bot.", ephemeral);
            return false;
        }
        return true;
    }

    private void loadAndPlayTracks(SlashCommandInteractionEvent event, MultipleMusicDto multipleMusicDto,
            AudioChannel userChannel, AudioChannel botChannel, boolean ephemeral) {
        GuildMusicManager musicManager = playerManagerService.getMusicManager(event.getGuild());
        utils.playerCleaner(musicManager);

        int trackCount = multipleMusicDto.getMusicDtoList().size();
        if (trackCount == 1) {
            playerManagerService.loadAndPlay(event, multipleMusicDto.getMusicDtoList().get(0), ephemeral);
        } else if (trackCount > 1) {
            playerManagerService.loadMultipleAndPlay(event, multipleMusicDto, ephemeral);
        } else {
            sendErrorMessage(event, "No tracks found.", ephemeral);
        }
    }

    private boolean shouldReplyEphemeral(SlashCommandInteractionEvent event) {
        var ephemeralOption = event.getOption("ephemeral");
        return ephemeralOption == null || ephemeralOption.getAsBoolean();
    }

    private boolean isInvalidInputCombination(OptionMapping queryOption, OptionMapping fileOption,
            SlashCommandInteractionEvent event, boolean ephemeral) {
        if (queryOption != null && fileOption != null) {
            sendErrorMessage(event, "Please provide either a query or upload a file, not both.", ephemeral);
            return true;
        }
        return false;
    }

    private MultipleMusicDto processInput(OptionMapping queryOption, OptionMapping fileOption) throws BadRequestException {
        if (queryOption != null) {
            return getSongUrl(queryOption.getAsString().trim());
        } else if (fileOption != null) {
            return processUploadedFile(fileOption);
        }
        return new MultipleMusicDto();
    }

    private MultipleMusicDto getSongUrl(String query) throws BadRequestException {
        List<MusicDto> musicDtos = new ArrayList<>();
        if (query.contains("https://www.youtube.com/shorts/"))
            query = youtubeShortsToVideo(query);
        if (isSupportedUrl(query)) {
            musicDtos.add(new MusicDto(null, query));
            return new MultipleMusicDto(1, musicDtos, 0);
        } else if (query.contains("https://open.spotify.com/")) {
            musicDtos = restService.getTracksFromSpotify(query);
            return restService.getYoutubeUrl(musicDtos);
        } else {
            return restService.getYoutubeUrl(new MusicDto(query, null));
        }
    }

    private MultipleMusicDto processUploadedFile(OptionMapping fileOption) {
        var musicDto = new MusicDto(fileOption.getAsAttachment().getFileName(), fileOption.getAsAttachment().getUrl());
        return new MultipleMusicDto(1, List.of(musicDto), 0);
    }

    private boolean isSupportedUrl(String url) {
        return (url.contains("https://www.youtube.com/watch?v=")
                || url.contains("https://youtu.be/")
                || url.contains("https://youtube.com/playlist?list=")
                || url.contains("https://music.youtube.com/watch?v=")
                || url.contains("https://music.youtube.com/playlist?list=")
                || url.contains("https://www.twitch.tv/")
                || url.contains("https://soundcloud.com/"));
    }

    private String youtubeShortsToVideo(String url) {
        return url.replace("shorts/", "watch?v=");
    }

    private void sendErrorMessage(SlashCommandInteractionEvent event, String message, boolean ephemeral) {
        EmbedBuilder embed = new EmbedBuilder()
                .setDescription(message)
                .setColor(Color.RED);
        event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(ephemeral).queue();
    }
}