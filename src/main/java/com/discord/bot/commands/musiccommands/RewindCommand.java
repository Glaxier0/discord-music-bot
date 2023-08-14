package com.discord.bot.commands.musiccommands;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class RewindCommand implements ISlashCommand {
    MusicCommandUtils utils;
    PlayerManagerService playerManagerService;

    public RewindCommand(PlayerManagerService playerManagerService, MusicCommandUtils utils) {
        this.playerManagerService = playerManagerService;
        this.utils = utils;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (utils.channelControl(event)) {
            var track = playerManagerService.getMusicManager(event).audioPlayer.getPlayingTrack();
            var seconds = event.getOption("sec").getAsLong();
            var songPosition = track.getPosition();
            if (songPosition - (seconds * 1000) < 0) {
                track.setPosition(0);
                event.replyEmbeds(new EmbedBuilder().setDescription("Song rewound to the start.")
                        .setColor(Color.GREEN).build()).queue();
            } else {
                track.setPosition(songPosition - (seconds * 1000));
                event.replyEmbeds(new EmbedBuilder().setDescription("Song rewound by " + seconds + " seconds.")
                        .setColor(Color.GREEN).build()).queue();
            }
        } else {
            event.replyEmbeds(new EmbedBuilder().setDescription("Please be in a same voice channel as bot.")
                    .setColor(Color.RED).build()).queue();
        }
    }
}

