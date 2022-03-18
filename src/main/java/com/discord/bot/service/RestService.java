package com.discord.bot.service;

import com.discord.bot.dao.pojo.MusicPojo;
import com.discord.bot.entity.MusicData;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
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
    public static String SPOTIFY_TOKEN;
    private final RestTemplate restTemplate;
    TrackService trackService;
    @Value("${youtube_api_key}")
    private String YOUTUBE_API_KEY;

    public RestService(TrackService trackService) {
        this.restTemplate = new RestTemplateBuilder().build();
        this.trackService = trackService;
    }

    public List<MusicPojo> getSpotifyMusicName(String spotifyUrl) {
        String id;
        List<MusicPojo> musicPojos = new ArrayList<>();

        if (spotifyUrl.contains("https://open.spotify.com/playlist/")) {
            id = spotifyUrl.substring(34, 56);
            spotifyUrl = "https://api.spotify.com/v1/playlists/" + id + "/tracks?fields=items(track(name,artists(name)))";

            JsonArray items = new JsonParser()
                    .parse(getSpotifyData(spotifyUrl).getBody()).getAsJsonObject().get("items").getAsJsonArray();

            for (int i = 0; i < items.size(); i++) {
                String musicName = items.get(i).getAsJsonObject().get("track").getAsJsonObject()
                        .getAsJsonArray("artists").get(0).getAsJsonObject().get("name").getAsString() +
                        " - " + items.get(i).getAsJsonObject().get("track").getAsJsonObject().get("name").getAsString();
                musicPojos.add(new MusicPojo(musicName, null));
            }
        } else if (spotifyUrl.contains("https://open.spotify.com/track/")) {
            id = spotifyUrl.substring(31, 53);
            spotifyUrl = "https://api.spotify.com/v1/tracks/" + id;

            ResponseEntity<String> responseEntity = getSpotifyData(spotifyUrl);
            String musicName = new JsonParser().parse(responseEntity.getBody()).getAsJsonObject()
                    .getAsJsonArray("artists").get(0).getAsJsonObject().get("name").getAsString() + " - " +
                    new JsonParser().parse(responseEntity.getBody()).getAsJsonObject().get("name").getAsString();
            musicPojos.add(new MusicPojo(musicName, null));
        }
        return musicPojos;
    }

    public ResponseEntity<String> getSpotifyData(String spotifyUrl) {
        URI spotifyUri = null;

        try {
            spotifyUri = new URI(spotifyUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(SPOTIFY_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(spotifyUri, HttpMethod.GET, entity, String.class);
        return responseEntity;
    }

    public MusicPojo getYoutubeLink(MusicPojo musicPojo) {
        MusicData musicData = trackService.findFirst1ByTitle(musicPojo.getTitle());
        if (musicData != null) {
            musicPojo.setYoutubeUri(musicData.getYoutubeUri());
        } else {
            String encodedMusicName;
            encodedMusicName = URLEncoder.encode(musicPojo.getTitle(), StandardCharsets.UTF_8);
            String youtubeUrl = "https://youtube.googleapis.com/youtube/v3/search?fields=items(id(videoId))&maxResults=1&q="
                    + encodedMusicName + "&key=" + YOUTUBE_API_KEY;
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
                musicPojo.setYoutubeUri("https://www.youtube.com/watch?v=" + videoId);
            } catch (HttpClientErrorException.Forbidden exception) {
                musicPojo.setYoutubeUri("403glaxierror");
            }
        }
        return musicPojo;
    }
}
