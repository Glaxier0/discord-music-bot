package com.discord.bot.commands.musiccommands;

import com.discord.bot.audioplayer.GuildMusicManager;
import com.discord.bot.entity.pojo.MusicPojo;
import com.discord.bot.loader.MusicLoader;
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

public class PlayCommand extends MusicPlayerCommand {
    private TrackService trackService;
    private RestService restService;
    private MusicLoader musicLoader;

    public PlayCommand(RestService restService, PlayerManagerService playerManagerService, TrackService trackService, MusicCommandUtils utils) {
        this.restService = restService;
        this.playerManagerService = playerManagerService;
        this.trackService = trackService;
        this.utils = utils;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String query = event.getOption("query").getAsString().trim();
        List<MusicPojo> musicPojos = musicLoader.loadMusicUsingQuery(restService, query, event.getChannel());
        playMusic(event, musicPojos);
    }

    private void playMusic(SlashCommandInteractionEvent event, List<MusicPojo> musicPojos) {
        AudioChannel userChannel = event.getMember().getVoiceState().getChannel();
        AudioChannel botChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();

        if (utils.isUserInVoiceChannel(event)) {
            int trackSize = musicPojos.size();
            if (trackSize != 0) {
                if (utils.isBotInVoiceChannel(event) == false) {
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
}
