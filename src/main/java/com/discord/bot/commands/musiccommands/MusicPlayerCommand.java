package com.discord.bot.commands.musiccommands;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public abstract class MusicPlayerCommand implements ISlashCommand {
    MusicCommandUtils utils;
    PlayerManagerService playerManagerService;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (isValidState(event)) {
            operate(event, embedBuilder);
        } else {
            event.replyEmbeds(embedBuilder.setDescription(getFailDescription())
                    .setColor(Color.RED).build()).queue();
        }
    }

    abstract void operate(SlashCommandInteractionEvent event, EmbedBuilder embedBuilder);
    abstract boolean isValidState(SlashCommandInteractionEvent event);
    abstract String getFailDescription();
}
