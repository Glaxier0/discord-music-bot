package com.discord.bot.commands.musiccommands;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

@AllArgsConstructor
public class ForwardCommand implements ISlashCommand {
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

            if (option != null) {
                var seconds = option.getAsInt();
                track.setPosition(track.getPosition() + (seconds * 1000L));

                embedBuilder.setDescription("Song forwarded by " + seconds + " seconds.").setColor(Color.GREEN);
            } else embedBuilder.setDescription("Seconds can't be null.").setColor(Color.RED);
        } else embedBuilder.setDescription("Please be in a same voice channel as bot.").setColor(Color.RED);

        event.replyEmbeds(embedBuilder.build()).setEphemeral(ephemeral).queue();
    }
}