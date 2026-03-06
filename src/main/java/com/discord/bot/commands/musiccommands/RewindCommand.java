package com.discord.bot.commands.musiccommands;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.ReplyService;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.discord.bot.audioplayer.GuildMusicManager;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

@AllArgsConstructor
public class RewindCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MusicCommandUtils utils;
    ReplyService replyService;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (utils.channelControl(event)) {
            GuildMusicManager musicManager = playerManagerService.getMusicManager(event.getGuild());
            var option = event.getOption("sec");
            var seconds = option != null ? option.getAsInt() : 0;

            musicManager.getPlayer().ifPresentOrElse(
                    (player) -> {
                        var track = player.getTrack();
                        if (track != null) {
                            long songPosition = player.getPosition();
                            long newPosition = Math.max(0, songPosition - (seconds * 1000L));
                            musicManager.getOrCreateLink().createOrUpdatePlayer()
                                    .setPosition(newPosition)
                                    .subscribe();
                        }
                    },
                    () -> {}
            );

            if (seconds * 1000L >= 0) {
                embedBuilder.setDescription("Song rewound by " + seconds + " seconds.").setColor(Color.GREEN);
            } else {
                embedBuilder.setDescription("Song rewound to the start.").setColor(Color.GREEN);
            }
        } else embedBuilder.setDescription("Please be in a same voice channel as bot.").setColor(Color.RED);

        replyService.replyWithEmbed(event, embedBuilder, utils.isEphemeralOptionEnabled(event));
    }
}