package com.discord.bot.commands;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public interface IButtonInteraction {
    void click(ButtonInteractionEvent event);
}
