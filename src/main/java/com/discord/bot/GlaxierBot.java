package com.discord.bot;

import com.discord.bot.commands.CommandManager;
import com.discord.bot.commands.JdaCommands;
import com.discord.bot.commands.AdminCommands;
import com.discord.bot.service.*;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class GlaxierBot {
    private final static Logger logger = LoggerFactory.getLogger(GlaxierBot.class);
    final RestService restService;
    final PlayerManagerService playerManagerService;
    final MusicCommandUtils musicCommandUtils;
    final SpotifyTokenService spotifyTokenService;

    @Value("${discord.bot.token}")
    private String discordToken;

    @Value("${discord.admin.server.id}")
    private String adminServerId;

    @Value("discord.admin.user.id")
    private String adminUserId;

    public GlaxierBot(RestService restService, PlayerManagerService playerManagerService,
                      MusicCommandUtils musicCommandUtils, SpotifyTokenService spotifyTokenService) {
        this.restService = restService;
        this.playerManagerService = playerManagerService;
        this.musicCommandUtils = musicCommandUtils;
        this.spotifyTokenService = spotifyTokenService;
    }

    @PostConstruct
    public void startDiscordBot() throws InterruptedException {
        JDA jda = JDABuilder.createDefault(discordToken)
                .addEventListeners(
                        new CommandManager(restService, playerManagerService, musicCommandUtils, adminUserId))
                .setActivity(Activity.listening("Type /mhelp")).build();
        jda.awaitReady();
        new JdaCommands().addJdaCommands(jda);
        new AdminCommands().addAdminCommands(jda, adminServerId);
        logger.info("Starting bot is done!");
    }

    @Scheduled(fixedDelay = 3500000)
    private void refreshSpotifyToken() {
        spotifyTokenService.getAccessToken();
    }
}