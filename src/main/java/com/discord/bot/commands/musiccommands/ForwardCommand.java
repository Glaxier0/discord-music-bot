package com.discord.bot.commands.musiccommands;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.ReplyService;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

@AllArgsConstructor
public class ForwardCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MusicCommandUtils utils;
    ReplyService replyService;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (utils.channelControl(event)) {
            var track = playerManagerService.getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack();
            var option = event.getOption("sec");
            var seconds = option != null ? option.getAsInt() : 0;
            track.setPosition(track.getPosition() + (seconds * 1000L));

            embedBuilder.setDescription("Song forwarded by " + seconds + " seconds.").setColor(Color.GREEN);
        } else embedBuilder.setDescription("Please be in a same voice channel as bot.").setColor(Color.RED);

        replyService.replyWithEmbed(event, embedBuilder, utils.isEphemeralOptionEnabled(event));
    }
}