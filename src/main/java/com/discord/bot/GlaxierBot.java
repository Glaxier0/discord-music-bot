package com.discord.bot;

import com.discord.bot.commands.CommandManager;
import com.discord.bot.commands.JdaCommands;
import com.discord.bot.commands.TestCommands;
import com.discord.bot.service.RestService;
import com.discord.bot.service.SpotifyTokenService;
import com.discord.bot.service.TrackService;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeHttpContextFilter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.security.auth.login.LoginException;

@Configuration
@EnableScheduling
public class GlaxierBot {
    RestService restService;
    PlayerManagerService playerManagerService;
    TrackService trackService;
    SpotifyTokenService spotifyTokenService;

    @Value("${discord_bot_token}")
    private String DISCORD_TOKEN;
    @Value("${test_server_id}")
    private String TEST_SERVER;
    @Value("${__Secure-3PSID}")
    private String PSID;
    @Value("${__Secure-3PAPISID}")
    private String PAPISID;


    public GlaxierBot(RestService restService, PlayerManagerService playerManagerService, TrackService trackService,
                      SpotifyTokenService spotifyTokenService) {
        this.restService = restService;
        this.playerManagerService = playerManagerService;
        this.trackService = trackService;
        this.spotifyTokenService = spotifyTokenService;
    }

    @Bean
    public void startDiscordBot() throws LoginException {
        JDA jda = JDABuilder.createDefault(DISCORD_TOKEN)
                .addEventListeners(
                        new CommandManager(restService, playerManagerService, trackService))
                .setActivity(Activity.listening("Type /mhelp")).build();
        new JdaCommands().addJdaCommands(jda);
        new TestCommands().addTestCommands(jda, TEST_SERVER);
        System.out.println("Starting bot is done!");
        ageRestriction();
    }

    @Scheduled(fixedDelay = 3500000)
    private void refreshSpotifyToken() {
        spotifyTokenService.getAccessToken();
    }

    private void ageRestriction() {
        YoutubeHttpContextFilter.setPSID(PSID);
        YoutubeHttpContextFilter.setPAPISID(PAPISID);
    }
}




