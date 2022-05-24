package com.discord.bot.service;

import com.discord.bot.entity.pojo.MusicPojo;
import com.discord.bot.entity.MusicData;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
public class SpotifyRestService {
    public static String SPOTIFY_TOKEN;
    private final RestTemplate restTemplate;
    TrackService trackService;

    private final String PLAYLIST_URL = "https://open.spotify.com/playlist/";
    private final String TRACK_URL = "https://open.spotify.com/track/";
    private final String API_PLAYLIST_URL = "https://api.spotify.com/v1/playlists/";

    private final String API_SINGLE_TRACK_URL = "https://api.spotify.com/v1/tracks/";
    private final String API_TRACK_URL = "/tracks?fields=items(track(name,artists(name)))";
    public SpotifyRestService(RestTemplate restTemplate, TrackService trackService) {
        this.restTemplate = restTemplate;
        this.trackService = trackService;
    }

    public List<MusicPojo> getSpotifyMusicName(String spotifyUrl) {
        List<MusicPojo> musicPojos = new ArrayList<>();
        final boolean isPlaylist = spotifyUrl.contains(PLAYLIST_URL);
        final boolean isSingleTrack = spotifyUrl.contains(TRACK_URL);

        if (isPlaylist) {
            musicPojos = getSpotifyPlayList(spotifyUrl);
        } else if (isSingleTrack) {
            musicPojos = getSpotifyTrack(spotifyUrl);
        }
        return musicPojos;
    }

    private List<MusicPojo> getSpotifyPlayList(String spotifyUrl) {
        List<MusicPojo> musicPojos = new ArrayList<>();
        String id = spotifyUrl.substring(34, 56);
        spotifyUrl = API_PLAYLIST_URL + id + API_TRACK_URL;
        ResponseEntity<String> responseEntity = getSpotifyData(spotifyUrl);
        JsonArray items = JsonParser.parseString(responseEntity.getBody()).getAsJsonObject().get("items").getAsJsonArray();

        for (int i = 0; i < items.size(); i++) {
            JsonObject trackObject = items.get(i).getAsJsonObject().get("track").getAsJsonObject();
            musicPojos.add(new MusicPojo(getMusicName(trackObject), null));
        }
        return musicPojos;
    }

    private List<MusicPojo> getSpotifyTrack(String spotifyUrl) {
        List<MusicPojo> musicPojos = new ArrayList<>();
        String id = spotifyUrl.substring(31, 53);
        spotifyUrl = API_SINGLE_TRACK_URL + id;
        ResponseEntity<String> responseEntity = getSpotifyData(spotifyUrl);

        JsonObject trackObject = JsonParser.parseString(responseEntity.getBody()).getAsJsonObject();
        musicPojos.add(new MusicPojo(getMusicName(trackObject), null));
        return musicPojos;
    }

    private String getMusicName(JsonObject trackObject) {
        return trackObject.getAsJsonArray("artists").get(0).getAsJsonObject().get("name").getAsString() +
                " - " + trackObject.get("name").getAsString();
    }

    private ResponseEntity<String> getSpotifyData(String spotifyUrl) {
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