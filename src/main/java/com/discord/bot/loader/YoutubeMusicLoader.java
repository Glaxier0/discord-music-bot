package com.discord.bot.loader;

import com.discord.bot.entity.pojo.MusicPojo;
import com.discord.bot.service.RestService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.ArrayList;
import java.util.List;

public class YoutubeMusicLoader {
    public List<MusicPojo> getMusicPojos(RestService restService, String query) {
        List<MusicPojo> musicPojos = new ArrayList<>();
        musicPojos.add(new MusicPojo(null, query));
        return musicPojos;
    }
}
