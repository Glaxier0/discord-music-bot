package com.discord.bot.loader;

import com.discord.bot.entity.pojo.MusicPojo;
import com.discord.bot.service.RestService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.ArrayList;
import java.util.List;

public class YoutubeMusicLoader extends MusicLoader{
    protected List<MusicPojo> getMusicInfo(RestService restService, String query, SlashCommandInteractionEvent event) {
        List<MusicPojo> musicPojos = new ArrayList<>();

        final boolean isYoutubeUrl = query.contains("https://www.youtube.com/watch?v=");

        if (isYoutubeUrl) {
            musicPojos.add(new MusicPojo(null, query));
        } else {
            MusicPojo musicPojo = restService.getYoutubeLink(new MusicPojo(query, null));
            final boolean isYoutubeHasError = musicPojo.getYoutubeUri().equals("403glaxierror");

            if (isYoutubeHasError) {
                apiLimitExceeded(event.getChannel());
            } else {
                musicPojos.add(musicPojo);
            }
        }

        return musicPojos;
    }
    protected List<MusicPojo> transformMusicPojo(List<MusicPojo> musicPojos) {
        return musicPojos;
    }
    private void apiLimitExceeded(MessageChannel channel) {
        channel.sendMessageEmbeds(new EmbedBuilder().setDescription("Youtube quota has exceeded. " +
                "Please use youtube links to play music for today.").build()).queue();
    }
}
