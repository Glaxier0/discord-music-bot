package com.discord.bot.commands.musiccommands;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class ForwardCommand implements ISlashCommand {
    MusicCommandUtils utils;
    PlayerManagerService playerManagerService;

    public ForwardCommand(PlayerManagerService playerManagerService, MusicCommandUtils utils) {
        this.playerManagerService = playerManagerService;
        this.utils = utils;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (utils.channelControl(event)) {
            var track = playerManagerService.getMusicManager(event).audioPlayer.getPlayingTrack();

            var option = event.getOption("sec");
            if (option == null) {
                event.replyEmbeds(new EmbedBuilder().setDescription("Seconds can't be null.")
                        .setColor(Color.RED).build()).queue();
                return;
            }

            var seconds = option.getAsLong();
            track.setPosition(track.getPosition() + (seconds * 1000));
            event.replyEmbeds(new EmbedBuilder().setDescription("Song forwarded by " + seconds + " seconds.")
                    .setColor(Color.GREEN).build()).queue();
        } else {
            event.replyEmbeds(new EmbedBuilder().setDescription("Please be in a same voice channel as bot.")
                    .setColor(Color.RED).build()).queue();
        }
    }
}

