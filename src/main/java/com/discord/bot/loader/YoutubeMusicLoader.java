package com.discord.bot.loader;

import com.discord.bot.entity.pojo.MusicPojo;
import com.discord.bot.service.RestService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.ArrayList;
import java.util.List;

public class YoutubeMusicLoader implements MusicLoader {
    private RestService restService;

    public YoutubeMusicLoader(RestService restService) {
        this.restService = restService;
    }

    @Override
    public List<MusicPojo> getMusicPojos(String query, MessageChannel channel) {
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
                apiLimitExceeded(channel);
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
