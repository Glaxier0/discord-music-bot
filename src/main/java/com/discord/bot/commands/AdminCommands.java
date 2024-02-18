package com.discord.bot.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminCommands {
    private static final Logger logger = LoggerFactory.getLogger(AdminCommands.class);

    public void addAdminCommands(JDA jda, String adminServerId) {
        Guild adminServer = jda.getGuildById(adminServerId);

        if (adminServer == null) {
            logger.error("Could not find the server with id: " + adminServerId);
            return;
        }

        var adminServerCommands = adminServer.updateCommands();

        adminServerCommands.addCommands(
                //admin commands
                Commands.slash("guilds", "Get guild list that bot is in."),
                Commands.slash("logs", "Get logs.")
        ).queue();

    }
}