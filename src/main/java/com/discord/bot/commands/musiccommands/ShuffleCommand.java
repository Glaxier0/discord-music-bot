package com.discord.bot.commands.musiccommands;

import com.discord.bot.audioplayer.GuildMusicManager;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.ReplyService;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.discord.bot.commands.ISlashCommand;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class ShuffleCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MusicCommandUtils utils;
    ReplyService replyService;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (utils.channelControl(event)) {
            GuildMusicManager musicManager = playerManagerService.getMusicManager(event.getGuild());
            List<AudioTrack> trackList = new ArrayList<>(musicManager.scheduler.queue);

            if (trackList.size() > 1) {
                Collections.shuffle(trackList);
                musicManager.scheduler.queue.clear();
                musicManager.scheduler.queueAll(trackList);

                embedBuilder.setDescription("Queue shuffled").setColor(Color.GREEN);
            } else embedBuilder.setDescription("Queue size have to be at least two.").setColor(Color.RED);
        } else embedBuilder.setDescription("Please be in a same voice channel as bot.").setColor(Color.RED);

        replyService.replyWithEmbed(event, embedBuilder, utils.isEphemeralOptionEnabled(event));
    }
}