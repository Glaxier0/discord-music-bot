package com.discord.bot.commands.musiccommands;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.ReplyService;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@AllArgsConstructor
public class MusicHelpCommand implements ISlashCommand {
    MusicCommandUtils musicCommandUtils;
    ReplyService replyService;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Music Commands").setDescription("""
                        - All bot replies will only visible to you if you don't set ephemeral as false.
                        - /play
                        - /skip
                        - /forward
                        - /rewind
                        - /pause
                        - /resume
                        - /leave
                        - /queue
                        - /swap
                        - /shuffle
                        - /loop
                        - /nowplaying
                        """);
        replyService.replyWithEmbed(event, embedBuilder, musicCommandUtils.isEphemeralOptionEnabled(event));
    }
}