package com.discord.bot.service;

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

@Service
public class RestService {

    private final RestTemplate restTemplate;
    @Value("${youtube_api_key}")
    private String YOUTUBE_API_KEY;
    @Value("${spotify_token}")
    private String SPOTIFY_TOKEN;

    public RestService() {
        this.restTemplate = new RestTemplateBuilder().build();
    }

    public ArrayList<String> getSpotifyMusicName(String spotifyUrl) {
        String id;
        ArrayList<String> youtubeLinks = new ArrayList<>();

        if (spotifyUrl.contains("https://open.spotify.com/playlist/")) {
            id = spotifyUrl.substring(34, 56);
            spotifyUrl = "https://api.spotify.com/v1/playlists/" + id + "/tracks?fields=items(track(name%2C%20artists(name)))";

            JsonArray items = new JsonParser()
                    .parse(getSpotifyData(spotifyUrl).getBody()).getAsJsonObject().get("items").getAsJsonArray();
            ArrayList<String> musics = new ArrayList<>();

            for (int i = 0; i < items.size(); i++) {
                musics.add(items.get(i).getAsJsonObject().get("track").getAsJsonObject()
                        .getAsJsonArray("artists").get(0).getAsJsonObject().get("name").getAsString() +
                        " " + items.get(i).getAsJsonObject().get("track").getAsJsonObject().get("name").getAsString());
                musics.set(i, URLEncoder.encode(musics.get(i), StandardCharsets.UTF_8));
            }
            youtubeLinks = getYoutubeLink(musics);
        } else if (spotifyUrl.contains("https://open.spotify.com/track/")) {
            id = spotifyUrl.substring(31, 53);
            spotifyUrl = "https://api.spotify.com/v1/tracks/" + id;

            ResponseEntity<String> responseEntity = getSpotifyData(spotifyUrl);
            String musicName = new JsonParser().parse(responseEntity.getBody()).getAsJsonObject()
                    .getAsJsonArray("artists").get(0).getAsJsonObject().get("name").getAsString() + " " +
                    new JsonParser().parse(responseEntity.getBody()).getAsJsonObject().get("name").getAsString();
            youtubeLinks.add(getYoutubeLink(musicName));
        }
        return youtubeLinks;
    }

    public ResponseEntity<String> getSpotifyData(String spotifyUrl) {
        URI uri = null;

        try {
            uri = new URI(spotifyUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(SPOTIFY_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
    }

    public String getYoutubeLink(String musicName) {
        musicName = URLEncoder.encode(musicName, StandardCharsets.UTF_8);
        String url = "https://youtube.googleapis.com/youtube/v3/search?fields=items(id(videoId))&maxResults=1&q="
                + musicName + "&key=" + YOUTUBE_API_KEY;
        URI uri = null;

        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        JsonElement jsonElement;

        try {
            jsonElement = new JsonParser().parse(restTemplate.getForObject(uri, String.class));
        } catch (HttpClientErrorException.Forbidden exception) {
            return "403glaxierror";
        }

        String videoId = jsonElement.getAsJsonObject().getAsJsonArray("items")
                .get(0).getAsJsonObject().getAsJsonObject("id").get("videoId").getAsString();
        return "https://www.youtube.com/watch?v=" + videoId;
    }

    public ArrayList<String> getYoutubeLink(ArrayList<String> musicNames) {
        ArrayList<String> youtubeLinks = new ArrayList<>();
        int counter = 0;

        for (String musicName : musicNames) {
            String url = "https://youtube.googleapis.com/youtube/v3/search?fields=items(id(videoId))&maxResults=1&q="
                    + musicName + "&key=" + YOUTUBE_API_KEY;
            URI uri = null;

            try {
                uri = new URI(url);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            JsonElement jsonElement = null;
            try {
                jsonElement = new JsonParser().parse(restTemplate.getForObject(uri, String.class));
            } catch (HttpClientErrorException.Forbidden exception) {
                counter++;
            }

            if (counter > 0) {
                youtubeLinks.add("403glaxierror");
                break;
            }

            String videoId = jsonElement.getAsJsonObject().getAsJsonArray("items")
                    .get(0).getAsJsonObject().getAsJsonObject("id").get("videoId").getAsString();

            youtubeLinks.add("https://www.youtube.com/watch?v=" + videoId);
        }
        return youtubeLinks;
    }
}
