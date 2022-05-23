package com.discord.bot.commands.admincommands;

import com.discord.bot.commands.ISlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public abstract class AdminCommand implements ISlashCommand {
    static String ADMIN_ID = "315403352496275456";

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getUser().getId().equals(ADMIN_ID)) {
            operate(event);
        }
    }

    abstract void operate(SlashCommandInteractionEvent event);
}
