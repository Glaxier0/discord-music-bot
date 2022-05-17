package com.discord.bot.service;

import com.discord.bot.entity.pojo.MusicPojo;
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
public class SpotifyRestSerivce {
    public static String SPOTIFY_TOKEN;
    private final RestTemplate restTemplate;
    TrackService trackService;

    public SpotifyRestService(TrackService trackService) {
        this.restTemplate = new RestTemplateBuilder().build();
        this.trackService = trackService;
    }

    public List<MusicPojo> getSpotifyMusicName(String spotifyUrl) {
        String id;
        List<MusicPojo> musicPojos = new ArrayList<>();

        if (spotifyUrl.contains("https://open.spotify.com/playlist/")) {
            musicPojos = getSpotifyPlayList(musicPojos, spotifyUrl);
        } else if (spotifyUrl.contains("https://open.spotify.com/track/")) {
            musicPojos = getSpotifyTrack(musicPojos, spotifyUrl);
        }
        return musicPojos;
    }

    public void getSpotifyPlayList (List<MusicPojo> musicPojos, String spotifyUrl) {
        id = spotifyUrl.substring(34, 56);
        spotifyUrl = "https://api.spotify.com/v1/playlists/" + id + "/tracks?fields=items(track(name,artists(name)))";
        ResponseEntity<String> spotifyData = getSpotifyData(spotifyUrl);
        JsonArray items = new JsonParser()
                .parse(spotifyData.getBody()).getAsJsonObject().get("items").getAsJsonArray();

        for (int i = 0; i < items.size(); i++) {
            String musicName = items.get(i).getAsJsonObject().get("track").getAsJsonObject()
                    .getAsJsonArray("artists").get(0).getAsJsonObject().get("name").getAsString() +
                    " - " + items.get(i).getAsJsonObject().get("track").getAsJsonObject().get("name").getAsString();
            musicPojos.add(new MusicPojo(musicName, null));
        }
    }

    public void getSpotifyTrack {List<MusicPojo> musicPojos, String spotifyUrl) {
        id = spotifyUrl.substring(31, 53);
        spotifyUrl = "https://api.spotify.com/v1/tracks/" + id;

        ResponseEntity<String> responseEntity = getSpotifyData(spotifyUrl);
        String musicName = new JsonParser().parse(responseEntity.getBody()).getAsJsonObject()
                .getAsJsonArray("artists").get(0).getAsJsonObject().get("name").getAsString() + " - " +
                new JsonParser().parse(responseEntity.getBody()).getAsJsonObject().get("name").getAsString();
        musicPojos.add(new MusicPojo(musicName, null));
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
}