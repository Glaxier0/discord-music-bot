package com.discord.bot.loader;

import com.discord.bot.entity.pojo.MusicPojo;
import com.discord.bot.service.RestService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.ArrayList;
import java.util.List;

public class MusicLoaderManager {
    public List<MusicPojo> loadMusicUsingQuery(RestService restService, String query, SlashCommandInteractionEvent event) {
        MusicLoader musicLoader = MusicLoaderFactory.createMusicLoader(query);
        return musicLoader.getMusicPojos(restService, query, event);
    }

}
