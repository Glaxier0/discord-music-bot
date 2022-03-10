package com.discord.bot.commands.musiccommands;

import com.discord.bot.audioplayer.GuildMusicManager;
import com.discord.bot.audioplayer.PlayerManager;
import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.RestService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PlayCommand implements ISlashCommand {
    RestService restService;
    MusicCommandUtils utils;

    public PlayCommand(RestService restService, MusicCommandUtils utils) {
        this.restService = restService;
        this.utils = utils;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String youtubeLink = event.getOption("query").getAsString().trim();
        List<String> youtubeLinks = getYoutubeLink(youtubeLink, event);

        playMusic(event, youtubeLinks);
    }

    private void playMusic(SlashCommandInteractionEvent event, List<String> youtubeLinks) {
        AudioChannel userChannel = event.getMember().getVoiceState().getChannel();
        AudioChannel botChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();
        boolean isUserInVoiceChannel = event.getMember().getVoiceState().inAudioChannel();
        boolean isBotInVoiceChannel = event.getGuild().getSelfMember().getVoiceState().inAudioChannel();

        if (isUserInVoiceChannel) {
            if (!youtubeLinks.isEmpty()) {
                if (!isBotInVoiceChannel) {
                    GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event);
                    musicManager.scheduler.player.destroy();
                    musicManager.scheduler.queue.clear();
                    event.getGuild().getAudioManager().openAudioConnection(userChannel);
                    botChannel = userChannel;
                }
                if (botChannel.equals(userChannel)) {
                    int trackSize = youtubeLinks.size();
                    if (trackSize > 1) {
                        PlayerManager.getInstance().loadMultipleAndPlay(event, youtubeLinks);
                    } else if (trackSize == 1) {
                        PlayerManager.getInstance().loadAndPlay(event, youtubeLinks.get(0));
                    }
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
}
