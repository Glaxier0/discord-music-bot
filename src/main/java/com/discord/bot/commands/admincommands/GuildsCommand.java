package com.discord.bot.commands.admincommands;

import com.discord.bot.commands.ISlashCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GuildsCommand implements ISlashCommand {
    String ADMIN = "315403352496275456";

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getUser().getId().equals(ADMIN)) {
            try {
                File guildsFile = new File("guilds.txt");
                BufferedWriter writer = new BufferedWriter(new FileWriter(guildsFile, true));
                writer.write("GUILD COUNT: " + event.getJDA().getGuilds().size() + "\n        ID         NAME");

                for (Guild guild : event.getJDA().getGuilds()) {
                    writer.append("\n" + guild.getId() + " " + guild.getName());
                }

                writer.close();
                event.replyFile(guildsFile).queue();
                Thread.sleep(100);
                guildsFile.delete();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
