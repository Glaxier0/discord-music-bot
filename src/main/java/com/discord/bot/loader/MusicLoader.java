package com.discord.bot.loader;

import com.discord.bot.entity.pojo.MusicPojo;
import com.discord.bot.service.RestService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.ArrayList;
import java.util.List;

public class MusicLoader {
    public List<MusicPojo> loadMusicUsingQuery(RestService restService, String query, MessageChannel channel) {
        List<MusicPojo> musicPojos = new ArrayList<>();
        final boolean isYoutubeUrl = query.contains("https://www.youtube.com/watch?v=");
        final boolean isSpotifyUrl = query.contains("https://open.spotify.com/");

        if (isYoutubeUrl) {
            musicPojos = new YoutubeMusicLoader().getMusicPojos(restService, query);
        } else if (isSpotifyUrl) {
            musicPojos = new SpotifyMusicLoader().getMusicPojos(restService, query);
        } else {
            MusicPojo musicPojo = restService.getYoutubeLink(new MusicPojo(query, null));
            final boolean isYoutubeHasError = musicPojo.getYoutubeUri().equals("403glaxierror");

            if (isYoutubeHasError) {
                apiLimitExceeded(channel);
            } else {
                musicPojos.add(musicPojo);
            }
        }

        return musicPojos;
    }

    private void apiLimitExceeded(MessageChannel channel) {
        channel.sendMessageEmbeds(new EmbedBuilder().setDescription("Youtube quota has exceeded. " +
                "Please use youtube links to play music for today.").build()).queue();
    }
}
