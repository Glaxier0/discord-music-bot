package com.discord.bot.commands.musiccommands.ChannelValid;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface ValidStrategy {
    boolean isValid(SlashCommandInteractionEvent event);
}
