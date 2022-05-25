package com.discord.bot.service;

import com.discord.bot.entity.pojo.MusicPojo;
import com.discord.bot.entity.MusicData;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class RestService {
    private final RestTemplate restTemplate;
    TrackService trackService;

    @Autowired
    private YoutubeRestService youtubeRestService;

    @Autowired
    private SpotifyRestService spotifyRestService;

    @Autowired
    public RestService(TrackService trackService) {
        this.restTemplate = new RestTemplateBuilder().build();
        this.trackService = trackService;
    }

    public MusicPojo getYoutubeLink(MusicPojo musicPojo) {
        return youtubeRestService.getYoutubeLink(musicPojo);
    }

    public List<MusicPojo> getSpotifyMusicName(String spotifyUrl) {
        return spotifyRestService.getSpotifyMusicName(spotifyUrl);
    }


}