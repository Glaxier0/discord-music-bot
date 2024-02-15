package com.discord.bot;

import com.discord.bot.commands.CommandManager;
import com.discord.bot.commands.JdaCommands;
import com.discord.bot.commands.TestCommands;
import com.discord.bot.service.*;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class GlaxierBot {
    final RestService restService;
    final PlayerManagerService playerManagerService;
    final MusicCommandUtils musicCommandUtils;
    final SpotifyTokenService spotifyTokenService;

    @Value("${discord_bot_token}")
    private String DISCORD_TOKEN;
    @Value("${test_server_id}")
    private String TEST_SERVER;

    public GlaxierBot(RestService restService, PlayerManagerService playerManagerService,
                      MusicCommandUtils musicCommandUtils, SpotifyTokenService spotifyTokenService) {
        this.restService = restService;
        this.playerManagerService = playerManagerService;
        this.musicCommandUtils = musicCommandUtils;
        this.spotifyTokenService = spotifyTokenService;
    }

    @PostConstruct
    public void startDiscordBot() {
        JDA jda = JDABuilder.createDefault(DISCORD_TOKEN)
                .addEventListeners(
                        new CommandManager(restService, playerManagerService, musicCommandUtils))
                .setActivity(Activity.listening("Type /mhelp")).build();
        new JdaCommands().addJdaCommands(jda);
        new TestCommands().addTestCommands(jda, TEST_SERVER);
        System.out.println("Starting bot is done!");
    }

    @Scheduled(fixedDelay = 3500000)
    private void refreshSpotifyToken() {
        spotifyTokenService.getAccessToken();
    }
}