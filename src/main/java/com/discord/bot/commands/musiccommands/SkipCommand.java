package com.discord.bot.commands.musiccommands;

import com.discord.bot.audioplayer.PlayerManagerService;
import com.discord.bot.commands.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class SkipCommand implements ISlashCommand {
    MusicCommandUtils utils;
    PlayerManagerService playerManagerService;

    public SkipCommand(PlayerManagerService playerManagerService, MusicCommandUtils utils) {
        this.playerManagerService = playerManagerService;
        this.utils = utils;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (utils.channelControl(event)) {
            playerManagerService.getMusicManager(event).scheduler.nextTrack();
            event.replyEmbeds(new EmbedBuilder().setDescription("Song skipped").setColor(Color.GREEN).build()).queue();
        } else {
            event.replyEmbeds(new EmbedBuilder().setDescription("Please be in a same voice channel as bot.")
                    .setColor(Color.RED).build()).queue();
        }
    }
}
