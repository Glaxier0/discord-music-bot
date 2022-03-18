package com.discord.bot.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class JdaCommands {
    public void addJdaCommands(JDA jda) {
        CommandListUpdateAction globalCommands = jda.updateCommands();
        globalCommands.addCommands(
                //Music Commands
                Commands.slash("play", "Play a song on your voice channel.")
                        .addOptions(new OptionData(OptionType.STRING, "query", "Song url or name.")
                                .setRequired(true)),
                Commands.slash("skip", "Skip the current song."),
                Commands.slash("pause", "Pause the current song."),
                Commands.slash("resume", "Resume paused song."),
                Commands.slash("leave", "Make bot leave voice channel."),
                Commands.slash("queue", "List song queue."),
                Commands.slash("swap", "Swap order of two songs in queue")
                        .addOptions(new OptionData(OptionType.INTEGER, "songnum1",
                                        "Song number in the queue to be changed.").setRequired(true),
                                new OptionData(OptionType.INTEGER, "songnum2",
                                        "Song number in queue to be changed.").setRequired(true)),
                Commands.slash("shuffle", "Shuffle the queue."),
                Commands.slash("loop", "Loop the current song."),
                Commands.slash("mhelp", "Help page for music commands.")
        ).queue();
    }
}
