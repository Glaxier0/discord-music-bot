package com.discord.bot.loader;

import com.discord.bot.entity.pojo.MusicPojo;
import com.discord.bot.service.RestService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class MusicLoader {
    public List<MusicPojo> getMusicPojos(RestService restService, String query, SlashCommandInteractionEvent event) {
        List<MusicPojo> musicPojos = new ArrayList<>();
        musicPojos = getMusicInfo(restService, query, event);
        musicPojos = transformMusicPojo(musicPojos);
        return musicPojos;
    }

    protected abstract List<MusicPojo> getMusicInfo(RestService restService, String query, SlashCommandInteractionEvent event);
    protected abstract List<MusicPojo> transformMusicPojo(List<MusicPojo> musicPojos);
}
