package com.discord.bot.commands.musiccommands;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.dto.MultipleMusicDto;
import com.discord.bot.dto.MusicDto;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.ReplyService;
import com.discord.bot.service.RestService;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
public class PlayCommand implements ISlashCommand {
    RestService restService;
    PlayerManagerService playerManagerService;
    MusicCommandUtils utils;
    ReplyService replyService;

    private static final Set<String> SUPPORTED_DOMAINS = Set.of(
            "youtube.com", "youtu.be", "soundcloud.com",
            "twitch.tv", "bandcamp.com", "vimeo.com",
            "mixcloud.com", "music.youtube.com");

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        boolean ephemeral = utils.isEphemeralOptionEnabled(event);
        event.deferReply(ephemeral).queue();

        var queryOption = event.getOption("query");
        var fileOption = event.getOption("file");

        if (isInvalidInputCombination(queryOption, fileOption, event, ephemeral))
            return;

        MultipleMusicDto multipleMusicDto = processInput(queryOption, fileOption);
        playMusic(event, multipleMusicDto, ephemeral);
    }

    private void playMusic(SlashCommandInteractionEvent event, MultipleMusicDto multipleMusicDto, boolean ephemeral) {
        AudioChannel userChannel = getAudioChannel(event, false);
        if (userChannel == null) {
            replyService.reply(event, "Please join a voice channel to play music.", Color.RED, ephemeral);
            return;
        }

        AudioChannel botChannel = getAudioChannel(event, true);
        if (!canPlayInChannel(userChannel, botChannel, event, ephemeral))
            return;

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
                replyService.reply(event, "Please check the bot's permissions in the voice channel.", Color.RED,
                        ephemeral);
                return false;
            }
            userChannel.getGuild().getAudioManager().openAudioConnection(userChannel);
        } else if (!botChannel.equals(userChannel)) {
            replyService.reply(event, "Please be in the same voice channel as the bot.", Color.RED, ephemeral);
            return false;
        }
        return true;
    }

    private void loadAndPlayTracks(SlashCommandInteractionEvent event, MultipleMusicDto multipleMusicDto,
            AudioChannel userChannel, AudioChannel botChannel, boolean ephemeral) {
        if (multipleMusicDto.hasError()) {
            replyService.reply(event, multipleMusicDto.getErrorMessage(), Color.RED, ephemeral);
            Guild guild = event.getGuild();
            utils.leaveIfEmpty(guild, playerManagerService.getMusicManager(guild));
            return;
        }

        int trackCount = multipleMusicDto.getMusicDtoList().size();
        if (trackCount == 1) {
            playerManagerService.loadAndPlay(event, multipleMusicDto.getMusicDtoList().get(0), ephemeral);
        } else if (trackCount > 1) {
            playerManagerService.loadMultipleAndPlay(event, multipleMusicDto, ephemeral);
        } else {
            replyService.reply(event, "No tracks found.", Color.RED, ephemeral);
        }
    }

    private boolean isInvalidInputCombination(OptionMapping queryOption, OptionMapping fileOption,
            SlashCommandInteractionEvent event, boolean ephemeral) {
        if (queryOption != null && fileOption != null) {
            replyService.reply(event, "Please provide either a query or upload a file, not both.", Color.RED,
                    ephemeral);
            return true;
        }
        return false;
    }

    private MultipleMusicDto processInput(OptionMapping queryOption,
            OptionMapping fileOption) {
        if (queryOption != null) {
            return getSongUrl(queryOption.getAsString().trim());
        } else if (fileOption != null) {
            var file = utils.getAttachedFile(fileOption);
            if (file == null) {
                return MultipleMusicDto.error("Invalid file upload. Please try again.");
            }
            return processUploadedFile(file);
        }
        return new MultipleMusicDto();
    }

    private MultipleMusicDto getSongUrl(String query) {
        if (isSupportedUrl(query)) {
            return MultipleMusicDto.of(List.of(new MusicDto(null, query)));
        } else if (query.contains("https://open.spotify.com/")) {
            return restService.getTracksFromSpotify(query);
        } else if (isUrl(query)) {
            return MultipleMusicDto.error("Please provide a valid YouTube search query or a supported URL.");
        } else {
            return MultipleMusicDto.of(List.of(new MusicDto(query, "ytsearch:" + query)));
        }
    }

    private MultipleMusicDto processUploadedFile(Attachment file) {
        var musicDto = new MusicDto(file.getFileName(), file.getUrl());
        return MultipleMusicDto.of(List.of(musicDto));
    }

    private boolean isSupportedUrl(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null)
                return false;

            for (String domain : SUPPORTED_DOMAINS) {
                if (host.contains(domain)) {
                    return true;
                }
            }
        } catch (URISyntaxException ignored) {
        }
        return false;
    }

    private boolean isUrl(String input) {
        try {
            new URI(input);
            return input.startsWith("http://") || input.startsWith("https://");
        } catch (URISyntaxException ignored) {
            return false;
        }
    }
}