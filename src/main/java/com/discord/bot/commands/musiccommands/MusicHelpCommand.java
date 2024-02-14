package com.discord.bot.commands.musiccommands;

import com.discord.bot.commands.ISlashCommand;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@NoArgsConstructor
public class MusicHelpCommand implements ISlashCommand {
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        var ephemeralOption = event.getOption("ephemeral");
        boolean ephemeral = ephemeralOption == null || ephemeralOption.getAsBoolean();

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
                        """)
                .setFooter("Bot can't play shorts.");

        event.replyEmbeds(embedBuilder.build()).setEphemeral(ephemeral).queue();
    }
}