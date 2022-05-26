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
public class YoutubeRestService {
    private RestTemplate restTemplate;
    TrackService trackService;
    @Value("${youtube_api_key}")
    private String YOUTUBE_API_KEY;
    private final String YOUTUBE_API_URL = "https://youtube.googleapis.com/youtube/v3/search?fields=items(id(videoId))&maxResults=1&q=";
    private final String YOUTUBE_VIDEO_URL = "https://www.youtube.com/watch?v=";

    @Autowired
    public YoutubeRestService(RestTemplate restTemplate, TrackService trackService) {
        this.restTemplate = restTemplate;
        this.trackService = trackService;
    }

    public MusicPojo getYoutubeLink(MusicPojo musicPojo) {
        MusicData musicData = trackService.findFirst1ByTitle(musicPojo.getTitle());
        if (musicData != null) {
            musicPojo.setYoutubeUri(musicData.getYoutubeUri());
        } else {
            String encodedMusicName = URLEncoder.encode(musicPojo.getTitle(), StandardCharsets.UTF_8);
            String youtubeUrl = YOUTUBE_API_URL + encodedMusicName + "&key=" + YOUTUBE_API_KEY;
            URI youtubeUri = null;

            try {
                youtubeUri = new URI(youtubeUrl);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            JsonElement jsonElement;
            try {
                jsonElement = new JsonParser().parse(restTemplate.getForObject(youtubeUri, String.class));
                String videoId = jsonElement.getAsJsonObject().getAsJsonArray("items")
                        .get(0).getAsJsonObject().getAsJsonObject("id").get("videoId").getAsString();
                musicPojo.setYoutubeUri(YOUTUBE_VIDEO_URL + videoId);
            } catch (HttpClientErrorException.Forbidden exception) {
                musicPojo.setYoutubeUri("403glaxierror");
            }
        }
        return musicPojo;
    }
}