package com.discord.bot.commands.musiccommands;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class NowPlayingCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MusicCommandUtils utils;

    public NowPlayingCommand(PlayerManagerService playerManagerService, MusicCommandUtils utils) {
        this.playerManagerService = playerManagerService;
        this.utils = utils;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (utils.channelControl(event)) {
            AudioTrack track = playerManagerService.getMusicManager(event).audioPlayer.getPlayingTrack();

            if (track != null) {
                embedBuilder.setTitle("Now playing").setDescription("[" + track.getInfo().title
                        + "](" + track.getInfo().uri + ")").setColor(Color.GREEN).build();
            } else {
                embedBuilder.setDescription("There is no song currently playing.").setColor(Color.RED);
            }
        } else {
            embedBuilder.setDescription("Please be in a same voice channel as bot.").setColor(Color.RED);
        }
        event.replyEmbeds(embedBuilder.build()).queue();
    }
}
