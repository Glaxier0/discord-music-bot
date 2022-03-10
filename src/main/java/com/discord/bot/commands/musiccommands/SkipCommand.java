package com.discord.bot.commands.musiccommands;

import com.discord.bot.audioplayer.PlayerManager;
import com.discord.bot.commands.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class SkipCommand implements ISlashCommand {
    MusicCommandUtils utils;

    public SkipCommand(MusicCommandUtils utils) {
        this.utils = utils;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        GuildVoiceState botVoiceState = event.getGuild().getSelfMember().getVoiceState();
        GuildVoiceState userVoiceState = event.getMember().getVoiceState();
        if (utils.channelControl(botVoiceState, userVoiceState)) {
            PlayerManager.getInstance().getMusicManager(event).scheduler.nextTrack();
            event.replyEmbeds(new EmbedBuilder().setDescription("Song skipped").setColor(Color.GREEN).build()).queue();
        } else {
            event.replyEmbeds(new EmbedBuilder().setDescription("Please be in a same voice channel as bot.")
                    .setColor(Color.RED).build()).queue();
        }
    }
}
