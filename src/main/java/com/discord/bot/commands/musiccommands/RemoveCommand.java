package com.discord.bot.commands.musiccommands;

import com.discord.bot.audioplayer.GuildMusicManager;
import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

@AllArgsConstructor
public class RemoveCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MusicCommandUtils utils;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        var ephemeralOption = event.getOption("ephemeral");
        boolean ephemeral = ephemeralOption == null || ephemeralOption.getAsBoolean();

        if (utils.channelControl(event)) {
            GuildMusicManager musicManager = playerManagerService.getMusicManager(event);
            var queue = musicManager.scheduler.queue;

            if (!queue.isEmpty()) {
                var command = event.getSubcommandName();

                if (command != null) {
                    embedBuilder = switch (command) {
                        case "single" -> utils.handleSingleCommand(event, queue, embedBuilder);
                        case "between" -> utils.handleBetweenCommand(event, queue, embedBuilder);
                        case "all" -> utils.handleAllCommand(queue, embedBuilder);
                        default -> embedBuilder;
                    };
                } else embedBuilder.setDescription("Please specify subcommand.").setColor(Color.RED);
            } else embedBuilder.setDescription("Song queue is empty.").setColor(Color.RED);
        } else embedBuilder.setDescription("Please be in a same voice channel as bot.").setColor(Color.RED);

        event.replyEmbeds(embedBuilder.build()).setEphemeral(ephemeral).queue();
    }
}