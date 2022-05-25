package com.discord.bot.commands.musiccommands;

import com.discord.bot.audioplayer.GuildMusicManager;
import com.discord.bot.commands.musiccommands.ChannelValid.IsUserInVoiceChannel;
import com.discord.bot.commands.musiccommands.ChannelValid.isBotInVoiceChannel;
import com.discord.bot.entity.pojo.MusicPojo;
import com.discord.bot.loader.MusicLoader;
import com.discord.bot.service.RestService;
import com.discord.bot.service.TrackService;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

public class PlayCommand extends MusicPlayerCommand {
    private TrackService trackService;
    private RestService restService;
    private String failDescription;

    public PlayCommand(RestService restService, PlayerManagerService playerManagerService, TrackService trackService, MusicCommandUtils utils) {
        this.restService = restService;
        this.playerManagerService = playerManagerService;
        this.trackService = trackService;
        this.utils = utils;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (isValidState(event)) {
            operate(event, embedBuilder);
        } else {
            event.replyEmbeds(embedBuilder.setDescription("Please be in same channel with bot.")
                    .build()).queue();
        }
    }

    @Override
    void operate(SlashCommandInteractionEvent event, EmbedBuilder embedBuilder) {
        String query = event.getOption("query").getAsString().trim();
        List<MusicPojo> musicPojos = new MusicLoader().loadMusicUsingQuery(restService, query, event);
        if (musicPojos.size() == 1) {
            if (musicPojos.get(0).getYoutubeUri() == null) {
                musicPojos.set(0, restService.getYoutubeLink(musicPojos.get(0)));
            }
            playerManagerService.loadAndPlay(event, musicPojos.get(0));
        } else {
            playerManagerService.loadMultipleAndPlay(event, musicPojos);
        }
    }

    @Override
    boolean isValidState(SlashCommandInteractionEvent event) {
        utils.setStrategy(new IsUserInVoiceChannel());
        if(!utils.isValid(event)){
            failDescription = "Please join to a voice channel.";
            return false;
        }
        String query = event.getOption("query").getAsString().trim();
        List<MusicPojo> musicPojos = new MusicLoader().loadMusicUsingQuery(restService, query, event);
        if(musicPojos.size() == 0){
            failDescription = "No tracks found.";
            return false;
        }
        AudioChannel userChannel = event.getMember().getVoiceState().getChannel();
        AudioChannel botChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();
        utils.setStrategy(new isBotInVoiceChannel());
        if (utils.isValid(event) == false) {
            GuildMusicManager musicManager = playerManagerService.getMusicManager(event);
            if (musicManager.scheduler.repeating) {
                musicManager.scheduler.repeating = false;
            }
            musicManager.scheduler.queue.clear();
            event.getGuild().getAudioManager().openAudioConnection(userChannel);
            botChannel = userChannel;
        }

        if (!botChannel.equals(userChannel)) {
            failDescription = "Please be in same channel with bot.";
            return false;
        }
        return true;
    }

    @Override
    String getFailDescription() {
        return failDescription;
    }

}