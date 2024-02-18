package com.discord.bot.commands.admincommands;

import com.discord.bot.commands.ISlashCommand;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;

@AllArgsConstructor
public class LogsCommand implements ISlashCommand {
    private final String adminUserId;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getUser().getId().equals(adminUserId)) {
            File logs = new File("logs.log");
            event.replyFiles(FileUpload.fromData(logs)).queue();
        }
    }
}
