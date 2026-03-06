package com.discord.bot.service;

import com.discord.bot.dto.response.spotify.SpotifyItemDto;
import com.discord.bot.dto.response.spotify.SpotifyPlaylistResponse;
import com.discord.bot.dto.response.spotify.SpotifyTrackResponse;
import com.discord.bot.dto.response.spotify.TrackDto;
import com.discord.bot.dto.MultipleMusicDto;
import com.discord.bot.dto.MusicDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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
    private final SearchSourceManager searchSourceManager;

    public RestService(SearchSourceManager searchSourceManager) {
        this.restTemplate = new RestTemplateBuilder().build();
        this.searchSourceManager = searchSourceManager;
    }

    public MultipleMusicDto getTracksFromSpotify(String spotifyUrl) {
        List<MusicDto> musicDtos = new ArrayList<>();
        String id = extractSpotifyId(spotifyUrl);
        String searchPrefix = searchSourceManager.isYoutubeSearchEnabled() ? "ytsearch:" : "scsearch:";

        if (id == null)
            return MultipleMusicDto.error("Invalid Spotify URL.");

        try {
            if (spotifyUrl.contains("/playlist/")) {
                String apiUrl = "https://api.spotify.com/v1/playlists/" + id
                        + "/tracks?fields=items(track(name,artists(name)))&limit=50";

                SpotifyPlaylistResponse playlist = getSpotifyPlaylistData(apiUrl);
                List<SpotifyItemDto> items = playlist.getSpotifyItemDtoList();

                if (items.isEmpty()) {
                    return MultipleMusicDto.error("This Spotify playlist appears to be empty.");
                }

                if (items.size() > 50) {
                    return MultipleMusicDto.error("Playlist is too large! Maximum 50 tracks allowed.");
                }

                for (SpotifyItemDto item : items) {
                    if (item == null || item.getTrackDtoList() == null)
                        continue;
                    TrackDto track = item.getTrackDtoList();
                    if (track.getArtistDtoList() != null && !track.getArtistDtoList().isEmpty()) {
                        String musicName = track.getArtistDtoList().get(0).getName() + " - " + track.getName();
                        musicDtos.add(new MusicDto(musicName, searchPrefix + musicName));
                    }
                }
                
            } else if (spotifyUrl.contains("/track/")) {
                String apiUrl = "https://api.spotify.com/v1/tracks/" + id;

                SpotifyTrackResponse track = getSpotifyTrackData(apiUrl);
                if (track == null || track.getArtistDtoList() == null || track.getArtistDtoList().isEmpty()) {
                    return MultipleMusicDto.error("Could not retrieve track information from Spotify.");
                }
                String musicName = track.getArtistDtoList().get(0).getName() + " - " + track.getSongName();
                musicDtos.add(new MusicDto(musicName, searchPrefix + musicName));
            } else {
                return MultipleMusicDto.error("Unsupported Spotify URL. Please provide a direct link to a track or playlist.");
            }
        } catch (Exception e) {
            logger.error("Unexpected error from Spotify API", e);
            return MultipleMusicDto.error("An unexpected error occurred while loading Spotify tracks.");
        }

        return MultipleMusicDto.of(musicDtos);
    }

    private SpotifyPlaylistResponse getSpotifyPlaylistData(String spotifyUrl) {
        try {
            URI spotifyUri = createUri(spotifyUrl);
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(spotifyToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            var response = restTemplate.exchange(spotifyUri, HttpMethod.GET, entity, SpotifyPlaylistResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            throw new HttpClientErrorException(response.getStatusCode(),
                    "Failed to fetch playlist data from Spotify.");
        } catch (RestClientException e) {
            logger.error("Error fetching Spotify playlist data", e);
            throw e;
        }
    }

    private SpotifyTrackResponse getSpotifyTrackData(String spotifyUrl) {
        try {
            URI spotifyUri = createUri(spotifyUrl);
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(spotifyToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            var response = restTemplate.exchange(spotifyUri, HttpMethod.GET, entity, SpotifyTrackResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            throw new SpotifyApiException("Failed to fetch track data from Spotify.");
        } catch (RestClientException e) {
            logger.error("Error fetching Spotify track data", e);
            throw e;
        }
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
}