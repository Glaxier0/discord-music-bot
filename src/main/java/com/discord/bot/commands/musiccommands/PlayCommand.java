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
        event.deferReply(ephemeral).queue();

        if (option != null) {
            String query = option.getAsString().trim();
            MultipleMusicDto multipleMusicDto = getSongUrl(query);
            if (multipleMusicDto.getCount() == 0 && multipleMusicDto.getFailCount() != 0) {
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                                .setDescription("Youtube quota has exceeded. " +
                                        "Please use youtube urls to play music for today.")
                                .setColor(Color.RED)
                                .build())
                        .setEphemeral(ephemeral)
                        .queue();
                return;
            }
            playMusic(event, multipleMusicDto, ephemeral);
        } else {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                            .setDescription("Option query can't be null.")
                            .setColor(Color.RED)
                            .build())
                    .setEphemeral(ephemeral)
                    .queue();
        }
    }

    private void playMusic(SlashCommandInteractionEvent event, MultipleMusicDto multipleMusicDto, boolean ephemeral) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        AudioChannel userChannel = getAudioChannel(event, false);
        AudioChannel botChannel = getAudioChannel(event, true);

        if (userChannel != null) {
            int trackSize = multipleMusicDto.getMusicDtoList().size();
            if (trackSize != 0) {
                if (botChannel == null) {
                    GuildMusicManager musicManager = playerManagerService.getMusicManager(event);
                    utils.playerCleaner(musicManager);

                    if (!userChannel.getGuild().getSelfMember().hasPermission(userChannel, Permission.VOICE_CONNECT)) {
                        event.getHook().sendMessageEmbeds(new EmbedBuilder()
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
                    if (trackSize == 1) playerManagerService.loadAndPlay(event, multipleMusicDto.getMusicDtoList()
                            .get(0), ephemeral);
                    else playerManagerService.loadMultipleAndPlay(event, multipleMusicDto, ephemeral);
                } else
                    embedBuilder.setDescription("Please be in the same voice channel as the bot.").setColor(Color.RED);
            } else embedBuilder.setDescription("No tracks found.").setColor(Color.RED);
        } else embedBuilder.setDescription("Please join a voice channel.").setColor(Color.RED);

        if (!embedBuilder.isEmpty())
            event.getHook().sendMessageEmbeds(embedBuilder.build()).setEphemeral(ephemeral).queue();
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

    private MultipleMusicDto getSongUrl(String query) {
        int count = 0;
        List<MusicDto> musicDtos = new ArrayList<>();
        if (isSupportedUrl(query)) {
            musicDtos.add(new MusicDto(null, query));
            count++;
            return new MultipleMusicDto(count, musicDtos, 0);
        } else if (query.contains("https://open.spotify.com/")) {
            musicDtos = restService.getTracksFromSpotify(query);
            return restService.getYoutubeUrl(musicDtos);
        } else {
            return restService.getYoutubeUrl(new MusicDto(query, null));
        }
    }

    private boolean isSupportedUrl(String url) {
        return (url.contains("https://www.youtube.com/watch?v=")
                || url.contains("https://youtu.be/")
                || url.contains("https://youtube.com/playlist?list=")
                || url.contains("https://music.youtube.com/watch?v=")
                || url.contains("https://music.youtube.com/playlist?list=")
                || url.contains("https://www.twitch.tv/")
                || url.contains("https://soundcloud.com/")
        );
    }
}