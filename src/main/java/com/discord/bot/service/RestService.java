package com.discord.bot.service;

import com.discord.bot.dto.response.spotify.SpotifyItemDto;
import com.discord.bot.dto.response.spotify.SpotifyPlaylistResponse;
import com.discord.bot.dto.response.spotify.SpotifyTrackResponse;
import com.discord.bot.dto.response.spotify.TrackDto;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import com.discord.bot.dto.MusicDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.awt.Color;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RestService {
    private final static Logger logger = LoggerFactory.getLogger(RestService.class);
    public static String spotifyToken;
    private final RestTemplate restTemplate;

    public RestService() {
        this.restTemplate = new RestTemplateBuilder().build();
    }

    public List<MusicDto> getTracksFromSpotify(SlashCommandInteractionEvent event, String spotifyUrl) {
        logger.info("Getting tracks from Spotify.");
        List<MusicDto> musicDtos = new ArrayList<>();
        String id = extractSpotifyId(spotifyUrl);
        if (id == null) {
            sendErrorMessage(event, "Invalid Spotify URL provided.", true);
            return List.of();
        }

        if (spotifyUrl.contains("/playlist/")) {
            String apiUrl = "https://api.spotify.com/v1/playlists/" + id + "/tracks?fields=items(track(name,artists(name)))&limit=50";

            SpotifyPlaylistResponse playlist = getSpotifyPlaylistData(apiUrl);
            List<SpotifyItemDto> items = playlist.getSpotifyItemDtoList();

            if (items.size() > 50) {
                sendErrorMessage(event, "Max allowed playlist size is 50.", true);
                return List.of();
            }

            for (SpotifyItemDto item : items) {
                TrackDto track = item.getTrackDtoList();
                String musicName = track.getArtistDtoList().get(0).getName() + " - " + track.getName();
                musicDtos.add(new MusicDto(musicName, "ytsearch:" + musicName));
            }
        } else if (spotifyUrl.contains("/track/")) {
            String apiUrl = "https://api.spotify.com/v1/tracks/" + id;

            SpotifyTrackResponse track = getSpotifyTrackData(apiUrl);
            String musicName = track.getArtistDtoList().get(0).getName() + " - " + track.getSongName();
            musicDtos.add(new MusicDto(musicName, "ytsearch:" + musicName));
        } else {
            sendErrorMessage(event, "Spotify search failed.", true);
            return List.of();
        }

        return musicDtos;
    }

    private SpotifyPlaylistResponse getSpotifyPlaylistData(String spotifyUrl) {
        URI spotifyUri = createUri(spotifyUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(spotifyToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(spotifyUri, HttpMethod.GET, entity, SpotifyPlaylistResponse.class).getBody();
    }

    private SpotifyTrackResponse getSpotifyTrackData(String spotifyUrl) {
        URI spotifyUri = createUri(spotifyUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(spotifyToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(spotifyUri, HttpMethod.GET, entity, SpotifyTrackResponse.class).getBody();
    }

    private URI createUri(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }

    private String extractSpotifyId(String spotifyUrl) {
        Pattern pattern = Pattern.compile("open\\.spotify\\.com/(playlist|track)/([a-zA-Z0-9]+)");
        Matcher matcher = pattern.matcher(spotifyUrl);

        if (matcher.find()) {
            return matcher.group(2); // group(1) = 'playlist' or 'track', group(2) = actual ID
        } else {
            logger.error("Invalid Spotify URL: " + spotifyUrl);
            return null;
        }
    }

    private void sendErrorMessage(SlashCommandInteractionEvent event, String message, boolean ephemeral) {
        EmbedBuilder embed = new EmbedBuilder()
                .setDescription(message)
                .setColor(Color.RED);
        event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(ephemeral).queue();
    }
}