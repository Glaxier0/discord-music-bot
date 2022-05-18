package com.discord.bot.loader;

import com.discord.bot.entity.pojo.MusicPojo;
import com.discord.bot.service.RestService;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.ArrayList;
import java.util.List;

public class SpotifyMusicLoader {
    public List<MusicPojo> getMusicPojos(RestService restService, String query) {
        List<MusicPojo> musicPojos = new ArrayList<>();
        musicPojos = spotifyToYoutube(restService, query);
        return musicPojos;
    }

    private List<MusicPojo> spotifyToYoutube(RestService restService, String spotifyUrl) {
        List<MusicPojo> musicPojoNameList = new ArrayList<>(restService.getSpotifyMusicName(spotifyUrl));
        return musicPojoNameList;
    }

}

