package com.discord.bot.commands.admincommands;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.ReplyService;
import com.discord.bot.service.SearchSourceManager;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

@AllArgsConstructor
public class SearchStatusCommand implements ISlashCommand {
    private final String adminUserId;
    private final SearchSourceManager searchSourceManager;
    private final ReplyService replyService;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!event.getUser().getId().equals(adminUserId)) {
            return;
        }

        event.deferReply(true).queue();

        boolean youtubeEnabled = searchSourceManager.isYoutubeSearchEnabled();
        String source = youtubeEnabled ? "YouTube" : "SoundCloud";
        String status = youtubeEnabled ? "ON" : "OFF";

        replyService.deferReply(event, "YouTube Search: **" + status + "**\nCurrent search source: **" + source + "**",
                Color.GREEN, true);
    }
}