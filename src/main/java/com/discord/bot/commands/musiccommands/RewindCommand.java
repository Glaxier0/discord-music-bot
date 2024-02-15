package com.discord.bot.commands.musiccommands;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

@AllArgsConstructor
public class RewindCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MusicCommandUtils utils;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        var ephemeralOption = event.getOption("ephemeral");
        boolean ephemeral = ephemeralOption == null || ephemeralOption.getAsBoolean();

        if (utils.channelControl(event)) {
            var track = playerManagerService.getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack();
            var option = event.getOption("sec");

            var seconds = option != null ? option.getAsInt() : 0;
            var songPosition = track.getPosition();
            if (songPosition - (seconds * 1000L) <= 0) {
                track.setPosition(0);
                embedBuilder.setDescription("Song rewound to the start.").setColor(Color.GREEN);
            } else {
                track.setPosition(songPosition - (seconds * 1000L));
                embedBuilder.setDescription("Song rewound by " + seconds + " seconds.").setColor(Color.GREEN);
            }
        } else embedBuilder.setDescription("Please be in a same voice channel as bot.").setColor(Color.RED);

        event.replyEmbeds(embedBuilder.build()).setEphemeral(ephemeral).queue();
    }
}