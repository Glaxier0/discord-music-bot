package com.discord.bot.commands.musiccommands;

import com.discord.bot.service.audioplayer.PlayerManagerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class LoopCommand extends MusicPlayerCommand {

    public LoopCommand(PlayerManagerService playerManagerService, MusicCommandUtils utils) {
        this.playerManagerService = playerManagerService;
        this.utils = utils;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (utils.channelControl(event)) {
            boolean repeat = playerManagerService.getMusicManager(event).scheduler.repeating;
            playerManagerService.getMusicManager(event).scheduler.repeating = !repeat;
            if (!repeat) {
                embedBuilder.setDescription(":white_check_mark: Track loop enabled.").setColor(Color.GREEN);
            } else {
                embedBuilder.setDescription(":x: Track loop disabled.").setColor(Color.RED);
            }
        } else {
            embedBuilder.setDescription("Please be in a same voice channel as bot.").setColor(Color.RED);
        }
        event.replyEmbeds(embedBuilder.build()).queue();
    }
}
