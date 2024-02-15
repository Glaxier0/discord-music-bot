package com.discord.bot.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class TestCommands {
    public void addTestCommands(JDA jda, String TEST_SERVER) {
        while (jda.getGuildById(TEST_SERVER) == null) {
            try {
                //noinspection BusyWait
                Thread.sleep(200);
            } catch (InterruptedException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }

        Guild testServer = jda.getGuildById(TEST_SERVER);
        if (testServer != null) {
            var testServerCommands = testServer.updateCommands();
            testServerCommands.addCommands(
                    //admin commands
                    Commands.slash("guilds", "Get guild list that bot is in."),
                    Commands.slash("logs", "Get logs.")
            ).queue();
        }
    }
}