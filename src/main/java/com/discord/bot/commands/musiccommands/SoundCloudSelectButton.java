package com.discord.bot.commands.musiccommands;

import com.discord.bot.commands.IButtonInteraction;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.ReplyService;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import dev.arbjerg.lavalink.client.player.Track;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.awt.*;
import java.util.List;

@AllArgsConstructor
public class SoundCloudSelectButton implements IButtonInteraction {
    private final PlayerManagerService playerManagerService;
    private final MusicCommandUtils musicCommandUtils;
    private final ReplyService replyService;

    @Override
    public void click(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        if (!componentId.startsWith("sc_")) {
            return;
        }

        int index;
        try {
            index = Integer.parseInt(componentId.substring(3));
        } catch (NumberFormatException e) {
            return;
        }

        var guild = event.getGuild();
        if (guild == null) return;

        long guildId = guild.getIdLong();
        String userId = event.getUser().getId();
        String key = guildId + "_" + userId;

        List<Track> pendingTracks = playerManagerService.getPendingSelection(key);
        if (pendingTracks == null || index >= pendingTracks.size()) {
            event.replyEmbeds(new EmbedBuilder()
                    .setDescription("Selection has expired. Please search again.")
                    .setColor(Color.RED)
                    .build()).setEphemeral(true).queue();
            return;
        }

        Track selectedTrack = pendingTracks.get(index);
        var musicManager = playerManagerService.getMusicManager(guild);
        musicManager.getScheduler().queue(selectedTrack);
        playerManagerService.removePendingSelection(key);

        event.editMessageEmbeds(new EmbedBuilder()
                .setDescription("Song added to the queue: " + selectedTrack.getInfo().getTitle()
                        + "\n in queue: " + musicManager.getScheduler().queue.size())
                .setColor(Color.GREEN)
                .build())
                .setComponents()
                .queue();
    }
}
