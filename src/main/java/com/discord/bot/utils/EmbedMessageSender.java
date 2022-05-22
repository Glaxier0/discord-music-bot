package com.discord.bot.utils;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class EmbedMessageSender {
    public static void sendReplyEmbed(SlashCommandInteractionEvent event, MessageEmbed messageEmbed) {
        event.replyEmbeds(messageEmbed).queue();
    }

    public static void sendEmbedToChannel(MessageChannel channel, MessageEmbed messageEmbed) {
        channel.sendMessageEmbeds(messageEmbed).queue();
    }
}


