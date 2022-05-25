package com.discord.bot.commands.musiccommands;

import com.discord.bot.commands.musiccommands.ChannelValid.ValidStrategy;
import com.discord.bot.commands.musiccommands.ChannelValid.isBotAndUserInSameChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class MusicCommandUtils {
    private ValidStrategy validStrategy = new isBotAndUserInSameChannel();
    /*여기서부터 시작*/
    public void setStrategy(){this.validStrategy = new isBotAndUserInSameChannel();}
    public void setStrategy(ValidStrategy validStrategy){
        this.validStrategy = validStrategy;
    }
    public boolean isValid(SlashCommandInteractionEvent event){
        return validStrategy.isValid(event);
    }
    /*public boolean isBotAndUserInSameChannel(SlashCommandInteractionEvent event) {
        if (!isBotInVoiceChannel(event)) {
            return false;
        }
        if (!isUserInVoiceChannel(event)) {
            return false;
        }
        GuildVoiceState selfVoiceState = event.getGuild().getSelfMember().getVoiceState();
        GuildVoiceState botVoiceState = event.getMember().getVoiceState();
        return selfVoiceState.getChannel() == botVoiceState.getChannel();
    }

    public boolean isBotInVoiceChannel(SlashCommandInteractionEvent event) {
        return event.getGuild().getSelfMember().getVoiceState().inAudioChannel();
    }

    public boolean isUserInVoiceChannel(SlashCommandInteractionEvent event) {
        return event.getMember().getVoiceState().inAudioChannel();
    }

    public void moveBotToUserChannel(SlashCommandInteractionEvent event, PlayerManagerService playerManagerService) {
        AudioChannel userChannel = event.getMember().getVoiceState().getChannel();
        AudioChannel botChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();
        GuildMusicManager musicManager = playerManagerService.getMusicManager(event);
        if (musicManager.scheduler.repeating) {
            musicManager.scheduler.repeating = false;
        }
        musicManager.scheduler.queue.clear();
        event.getGuild().getAudioManager().openAudioConnection(userChannel);
        botChannel = userChannel;
    }*/

}
