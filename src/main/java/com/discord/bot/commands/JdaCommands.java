package com.discord.bot.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
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
                Commands.slash("forward", "Forward the current song x seconds.")
                        .addOptions(new OptionData(OptionType.INTEGER, "sec", "seconds")
                                .setRequired(true)),
                Commands.slash("rewind", "Rewind the current song x seconds.")
                        .addOptions(new OptionData(OptionType.INTEGER, "sec", "seconds")
                                .setRequired(true)),
                Commands.slash("pause", "Pause the current song."),
                Commands.slash("resume", "Resume the paused song."),
                Commands.slash("leave", "Make bot leave the voice channel."),
                Commands.slash("queue", "List the song queue.")
                        .addOption(OptionType.INTEGER, "page", "Displayed page of the queue.", false),
                Commands.slash("swap", "Swap order of two the songs in queue")
                        .addOptions(new OptionData(OptionType.INTEGER, "songnum1",
                                        "Song number in the queue to be changed.").setRequired(true),
                                new OptionData(OptionType.INTEGER, "songnum2",
                                        "Song number in the queue to be changed.").setRequired(true)),
                Commands.slash("shuffle", "Shuffle the queue."),
                Commands.slash("loop", "Loop the current song."),
                Commands.slash("remove", "Remove song(s) from the queue.")
                        .addSubcommands(new SubcommandData("single", "Remove a song from the queue.")
                                        .addOption(OptionType.INTEGER, "songnum", "Song number to be removed from queue"),
                                new SubcommandData("between", "Removes songs at the specified indexes as " +
                                        "well as the songs located between those indexes")
                                        .addOptions(new OptionData(OptionType.INTEGER, "songnum1",
                                                        "The song number in the queue to be at the head of the removed list.")
                                                        .setRequired(true),
                                                new OptionData(OptionType.INTEGER, "songnum2",
                                                        "The song number in the queue to be at the tail of the removed list.")
                                                        .setRequired(true)),
                                new SubcommandData("all", "Clear the queue.")),
                Commands.slash("nowplaying", "Show the currently playing song."),
                Commands.slash("mhelp", "Help page for the music commands.")
        ).queue();
    }
}
