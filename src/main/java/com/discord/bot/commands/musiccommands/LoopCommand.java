package com.discord.bot.commands.musiccommands;

import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.ReplyService;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.discord.bot.commands.ISlashCommand;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

@AllArgsConstructor
public class LoopCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MusicCommandUtils utils;
    ReplyService replyService;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (utils.channelControl(event)) {
            boolean repeat = playerManagerService.getMusicManager(event.getGuild()).scheduler.repeating;
            playerManagerService.getMusicManager(event.getGuild()).scheduler.repeating = !repeat;

            if (!repeat) embedBuilder.setDescription(":white_check_mark: Track loop enabled.").setColor(Color.GREEN);
            else embedBuilder.setDescription(":x: Track loop disabled.").setColor(Color.RED);
        } else embedBuilder.setDescription("Please be in a same voice channel as bot.").setColor(Color.RED);

        replyService.replyWithEmbed(event, embedBuilder, utils.isEphemeralOptionEnabled(event));
    }
}