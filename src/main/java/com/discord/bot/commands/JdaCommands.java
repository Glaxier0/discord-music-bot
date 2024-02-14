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
        String ephemeralString = "Bot reply will only visible to you if set as TRUE, default value is TRUE.";

        globalCommands.addCommands(
                //Music Commands
                Commands.slash("play", "Play a song on your voice channel.")
                        .addOptions(new OptionData(OptionType.STRING, "query", "Song url or name.")
                                        .setRequired(true),
                                new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                        .setRequired(false)),
                Commands.slash("skip", "Skip the current song.")
                        .addOptions(new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                .setRequired(false)),
                Commands.slash("forward", "Forward the current song x seconds.")
                        .addOptions(new OptionData(OptionType.INTEGER, "sec", "seconds")
                                        .setMinValue(1)
                                        .setRequired(true),
                                new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                        .setRequired(false)),
                Commands.slash("rewind", "Rewind the current song x seconds.")
                        .addOptions(new OptionData(OptionType.INTEGER, "sec", "seconds")
                                        .setMinValue(1)
                                        .setRequired(true),
                                new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                        .setRequired(false)),
                Commands.slash("pause", "Pause the current song.")
                        .addOptions(new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                .setRequired(false)),
                Commands.slash("resume", "Resume the paused song.")
                        .addOptions(new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                .setRequired(false)),
                Commands.slash("leave", "Make bot leave the voice channel.")
                        .addOptions(new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                .setRequired(false)),
                Commands.slash("queue", "List the song queue.")
                        .addOptions(new OptionData(OptionType.INTEGER, "page", "Displayed page of the queue.")
                                        .setMinValue(1)
                                        .setRequired(false),
                                new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                        .setRequired(false)),
                Commands.slash("swap", "Swap order of two the songs in queue")
                        .addOptions(new OptionData(OptionType.INTEGER, "songnum1",
                                        "Song number in the queue to be changed.")
                                        .setMinValue(1)
                                        .setRequired(true),
                                new OptionData(OptionType.INTEGER, "songnum2",
                                        "Song number in the queue to be changed.")
                                        .setMinValue(1)
                                        .setRequired(true),
                                new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                        .setRequired(false)),
                Commands.slash("shuffle", "Shuffle the queue.")
                        .addOptions(new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                .setRequired(false)),
                Commands.slash("loop", "Loop the current song.")
                        .addOptions(new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                .setRequired(false)),
                Commands.slash("remove", "Remove song(s) from the queue.")
                        .addSubcommands(new SubcommandData("single", "Remove a song from the queue.")
                                        .addOptions(new OptionData(OptionType.INTEGER, "songnum", "Song number to be removed from queue")
                                                        .setMinValue(1)
                                                        .setRequired(false),
                                                new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                                        .setRequired(false)),
                                new SubcommandData("between", "Removes songs at the specified indexes as " +
                                        "well as the songs located between those indexes")
                                        .addOptions(new OptionData(OptionType.INTEGER, "songnum1",
                                                        "The song number in the queue to be at the head of the removed list.")
                                                        .setMinValue(1)
                                                        .setRequired(true),
                                                new OptionData(OptionType.INTEGER, "songnum2",
                                                        "The song number in the queue to be at the tail of the removed list.")
                                                        .setMinValue(1)
                                                        .setRequired(true),
                                                new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                                        .setRequired(false)),
                                new SubcommandData("all", "Clear the queue.")),
                Commands.slash("nowplaying", "Show the currently playing song.")
                        .addOptions(new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                .setRequired(false)),
                Commands.slash("mhelp", "Help page for the music commands.")
                        .addOptions(new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                .setRequired(false))
        ).queue();
    }
}
