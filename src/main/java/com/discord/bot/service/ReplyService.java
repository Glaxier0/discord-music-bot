package com.discord.bot.service;

import java.awt.Color;
import java.util.List;

import org.springframework.stereotype.Service;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;

@Service
public class ReplyService {
    public void reply(SlashCommandInteractionEvent event, String message, Color color, boolean ephemeral) {
        replyWithEmbed(event, createBasicEmbed(message, color), ephemeral);
    }

    public void replyWithEmbed(SlashCommandInteractionEvent event, EmbedBuilder embed, boolean ephemeral) {
        event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(ephemeral).queue();
    }

    public EmbedBuilder createBasicEmbed(String message, Color color) {
        return new EmbedBuilder()
                .setDescription(message)
                .setColor(color);
    }

    public void replyWithComponents(SlashCommandInteractionEvent event,
            EmbedBuilder embed,
            List<ActionRow> actionRows,
            boolean ephemeral) {
        event.getHook()
                .sendMessageEmbeds(embed.build())
                .setComponents(actionRows)
                .setEphemeral(ephemeral)
                .queue();
    }
}