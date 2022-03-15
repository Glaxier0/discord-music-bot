package com.discord.bot;

import com.discord.bot.audioplayer.PlayerManagerService;
import com.discord.bot.commands.CommandManager;
import com.discord.bot.service.RestService;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeHttpContextFilter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.security.auth.login.LoginException;

@Configuration
@EnableScheduling
public class GlaxierBot {
    RestService restService;
    PlayerManagerService playerManagerService;

    @Value("${discord_bot_token}")
    private String DISCORD_TOKEN;
    @Value("${test_server_id}")
    private String TEST_SERVER;
    @Value("${__Secure-3PSID}")
    private String PSID;
    @Value("${__Secure-3PAPISID}")
    private String PAPISID;


    public GlaxierBot(RestService restService, PlayerManagerService playerManagerService) {
        this.restService = restService;
        this.playerManagerService = playerManagerService;
    }

    @Bean
    public void startDiscordBot() throws LoginException {
        JDA jda = JDABuilder.createDefault(DISCORD_TOKEN)
                .addEventListeners(
                        new CommandManager(restService, playerManagerService))
                .setActivity(Activity.listening("Type /mhelp")).build();
        addCommands(jda);
        System.out.println("Starting bot is done!");
        ageRestriction();
    }

    private void addCommands(JDA jda) {
        while (jda.getGuildById(TEST_SERVER) == null) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Guild testServer = jda.getGuildById(TEST_SERVER);

        CommandListUpdateAction testServerCommands = testServer.updateCommands();
        CommandListUpdateAction globalCommands = jda.updateCommands();

        testServerCommands.addCommands(
                //admin commands
                Commands.slash("guilds", "Get guild list that bot is in."),
                Commands.slash("logs", "Get logs.")
        ).queue();

        globalCommands.addCommands(
                //Music Commands
                Commands.slash("play", "Play a song on your voice channel.")
                        .addOptions(new OptionData(OptionType.STRING, "query", "Song url or name.")
                                .setRequired(true)),
                Commands.slash("skip", "Skip the current song."),
                Commands.slash("pause", "Pause the current song."),
                Commands.slash("resume", "Resume paused song."),
                Commands.slash("leave", "Make bot leave voice channel."),
                Commands.slash("queue", "List song queue."),
                Commands.slash("swap", "Swap order of two songs in queue")
                        .addOptions(new OptionData(OptionType.INTEGER, "songnum1",
                                        "Song number in the queue to be changed.").setRequired(true),
                                new OptionData(OptionType.INTEGER, "songnum2",
                                        "Song number in queue to be changed.").setRequired(true)),
                Commands.slash("shuffle", "Shuffle the queue."),
                Commands.slash("loop", "Loop the current song."),
                Commands.slash("mhelp", "Help page for music commands.")
        ).queue();
    }

    private void ageRestriction() {
        YoutubeHttpContextFilter.setPSID(PSID);
        YoutubeHttpContextFilter.setPAPISID(PAPISID);
    }
}




