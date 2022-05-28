package com.discord.bot.commands.musiccommands;

import com.discord.bot.commands.musiccommands.ChannelValid.ValidStrategy;
import com.discord.bot.commands.musiccommands.ChannelValid.IsBotAndUserInSameChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ChannelValidation {
    private ValidStrategy validStrategy = new IsBotAndUserInSameChannel();
    /*여기서부터 시작*/
    public void setStrategy(){this.validStrategy = new IsBotAndUserInSameChannel();}
    public void setStrategy(ValidStrategy validStrategy){
        this.validStrategy = validStrategy;
    }
    public boolean isValid(SlashCommandInteractionEvent event){
        return validStrategy.isValid(event);
    }

}
