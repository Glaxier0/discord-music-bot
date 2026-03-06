package com.discord.bot.commands.musiccommands;

import com.discord.bot.audioplayer.GuildMusicManager;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.ReplyService;
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
    private final ReplyService replyService;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        Guild guild = event.getGuild();
        if (guild == null) {
            embedBuilder.setDescription("This command can only be used in a server.")
                        .setColor(Color.RED);
            replyService.replyWithEmbed(event, embedBuilder, utils.isEphemeralOptionEnabled(event));
            return;
        }

        if (utils.channelControl(event)) {
            GuildMusicManager musicManager = playerManagerService.getMusicManager(guild);
            utils.playerCleaner(musicManager);
            event.getJDA().getDirectAudioController().disconnect(guild);

            embedBuilder.setDescription("Bye.").setColor(Color.GREEN);
        } else {
            embedBuilder.setDescription("Please be in the same voice channel as the bot.")
                        .setColor(Color.RED);
        }

        replyService.replyWithEmbed(event, embedBuilder, utils.isEphemeralOptionEnabled(event));
    }
}
