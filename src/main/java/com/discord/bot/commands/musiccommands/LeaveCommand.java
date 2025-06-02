package com.discord.bot.commands.musiccommands;

import com.discord.bot.audioplayer.GuildMusicManager;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.discord.bot.commands.ISlashCommand;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

@AllArgsConstructor
public class LeaveCommand implements ISlashCommand {
    private final PlayerManagerService playerManagerService;
    private final MusicCommandUtils utils;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var ephemeralOption = event.getOption("ephemeral");
        boolean ephemeral = ephemeralOption == null || ephemeralOption.getAsBoolean();
        EmbedBuilder embedBuilder = new EmbedBuilder();

        Guild guild = event.getGuild();
        if (guild == null) {
            embedBuilder.setDescription("This command can only be used in a server.")
                        .setColor(Color.RED);
            event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
            return;
        }

        if (utils.channelControl(event)) {
            GuildMusicManager musicManager = playerManagerService.getMusicManager(guild);
            utils.playerCleaner(musicManager);
            guild.getAudioManager().closeAudioConnection();

            embedBuilder.setDescription("Bye.").setColor(Color.GREEN);
        } else {
            embedBuilder.setDescription("Please be in the same voice channel as the bot.")
                        .setColor(Color.RED);
        }

        event.replyEmbeds(embedBuilder.build()).setEphemeral(ephemeral).queue();
    }
}
