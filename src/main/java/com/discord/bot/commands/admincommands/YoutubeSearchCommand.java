package com.discord.bot.commands.admincommands;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.ReplyService;
import com.discord.bot.service.SearchSourceManager;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

@AllArgsConstructor
public class YoutubeSearchCommand implements ISlashCommand {
    private final String adminUserId;
    private final SearchSourceManager searchSourceManager;
    private final ReplyService replyService;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!event.getUser().getId().equals(adminUserId)) {
            return;
        }

        event.deferReply(true).queue();

        var enabledOption = event.getOption("enabled");

        boolean enabled = enabledOption.getAsBoolean();
        searchSourceManager.setYoutubeSearchEnabled(enabled);

        String source = enabled ? "YouTube" : "SoundCloud";
        replyService.deferReply(event, "Search source set to: " + source, Color.GREEN, true);
    }
}
