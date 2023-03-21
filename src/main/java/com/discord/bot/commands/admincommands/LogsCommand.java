package com.discord.bot.commands.admincommands;

import com.discord.bot.commands.ISlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;

public class LogsCommand implements ISlashCommand {
    String ADMIN = "315403352496275456";

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getUser().getId().equals(ADMIN)) {
            File logs = new File("logs.log");
            event.replyFiles(FileUpload.fromData(logs)).queue();
        }
    }
}
