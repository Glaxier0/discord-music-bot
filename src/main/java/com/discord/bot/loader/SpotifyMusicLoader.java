package com.discord.bot.loader;

import com.discord.bot.entity.pojo.MusicPojo;
import com.discord.bot.service.RestService;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.ArrayList;
import java.util.List;

public class SpotifyMusicLoader extends MusicLoader {
    @Override
    protected List<MusicPojo> getMusicInfo(RestService restService, String query, SlashCommandInteractionEvent event) {
        return restService.getSpotifyMusicName(query);
    }
    @Override
    protected List<MusicPojo> transformMusicPojo(RestService restService, SlashCommandInteractionEvent event, List<MusicPojo> musicPojos) {
        for (int i = 0; i < musicPojos.size(); i++) {
            musicPojos.set(i, restService.getYoutubeLink(musicPojos.get(i)));
        }
        return musicPojos;
    }

}

