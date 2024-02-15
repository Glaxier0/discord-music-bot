package com.discord.bot.service;

import com.discord.bot.dto.response.spotify.SpotifyItemDto;
import com.discord.bot.dto.response.spotify.SpotifyPlaylistResponse;
import com.discord.bot.dto.response.spotify.SpotifyTrackResponse;
import com.discord.bot.dto.response.spotify.TrackDto;
import com.discord.bot.dto.response.youtube.YoutubeResponse;
import com.discord.bot.repository.MusicRepository;
import com.discord.bot.dto.MultipleMusicDto;
import com.discord.bot.dto.MusicDto;
import com.discord.bot.entity.Music;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@SuppressWarnings("CanBeFinal")
@Service
public class RestService {
    public static String SPOTIFY_TOKEN;
    private final RestTemplate restTemplate;
    MusicRepository musicRepository;
    @Value("${youtube_api_key}")
    private String YOUTUBE_API_KEY;

    public RestService(MusicRepository musicRepository) {
        this.restTemplate = new RestTemplateBuilder().build();
        this.musicRepository = musicRepository;
    }

    public List<MusicDto> getTracksFromSpotify(String spotifyUrl) {
        String id;
        List<MusicDto> musicDtos = new ArrayList<>();

        if (spotifyUrl.contains("https://open.spotify.com/playlist/")) {
            id = spotifyUrl.substring(34, 56);
            spotifyUrl = "https://api.spotify.com/v1/playlists/" + id + "/tracks?fields=items(track(name,artists(name)))";
            SpotifyPlaylistResponse spotifyPlaylistResponse = getSpotifyPlaylistData(spotifyUrl);
            List<SpotifyItemDto> items = spotifyPlaylistResponse.getSpotifyItemDtoList();

            for (SpotifyItemDto item : items) {
                TrackDto trackDtoList = item.getTrackDtoList();
                String musicName = trackDtoList.getArtistDtoList().get(0).getName() + " - " + trackDtoList.getName();
                musicDtos.add(new MusicDto(musicName, null));
            }
        } else if (spotifyUrl.contains("https://open.spotify.com/track/")) {
            id = spotifyUrl.substring(31, 53);
            spotifyUrl = "https://api.spotify.com/v1/tracks/" + id;
            SpotifyTrackResponse spotifyTrackResponse = getSpotifyTrackData(spotifyUrl);
            String musicName = spotifyTrackResponse.getArtistDtoList().get(0).getName() +
                    " - " + spotifyTrackResponse.getSongName();
            musicDtos.add(new MusicDto(musicName, null));
        }

        return musicDtos;
    }

    public MultipleMusicDto getYoutubeUrl(MusicDto musicDto) {
        int count = 0;
        int failCount = 0;

        Music music = musicRepository.findFirstByTitle(musicDto.getTitle());

        if (music != null) {
            musicDto.setYoutubeUri(music.getYoutubeUri());
            count++;
        } else {
            try {
                var youtubeUri = getYoutubeApiUri(musicDto.getTitle());
                setYoutubeVideoUrl(youtubeUri, musicDto);
                count++;
            } catch (HttpClientErrorException.Forbidden e) {
                failCount++;
            }
        }

        return new MultipleMusicDto(count, Collections.singletonList(musicDto), failCount);
    }

    public MultipleMusicDto getYoutubeUrl(List<MusicDto> musicDtos) {
        AtomicInteger count = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        List<MusicDto> updatedMusicDtos = musicDtos.parallelStream().peek(musicDto -> {
            Music music = musicRepository.findFirstByTitle(musicDto.getTitle());

            if (music != null) {
                musicDto.setYoutubeUri(music.getYoutubeUri());
                count.incrementAndGet();
            } else {
                try {
                    var youtubeApiUri = getYoutubeApiUri(musicDto.getTitle());
                    setYoutubeVideoUrl(youtubeApiUri, musicDto);
                    count.incrementAndGet();
                } catch (HttpClientErrorException.Forbidden e) {
                    failCount.incrementAndGet();
                }
            }
        }).collect(Collectors.toList());

        return new MultipleMusicDto(count.get(), updatedMusicDtos, failCount.get());
    }

    private void setYoutubeVideoUrl(URI youtubeUri, MusicDto musicDto) {
        YoutubeResponse youtubeResponse = restTemplate.getForObject(youtubeUri, YoutubeResponse.class);
        assert youtubeResponse != null;
        musicDto.setYoutubeUri("https://www.youtube.com/watch?v=" + youtubeResponse.getItems().get(0).getId().getVideoId());
    }

    private SpotifyPlaylistResponse getSpotifyPlaylistData(String spotifyUrl) {
        URI spotifyUri = createUri(spotifyUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(SPOTIFY_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(spotifyUri, HttpMethod.GET, entity, SpotifyPlaylistResponse.class).getBody();
    }

    private SpotifyTrackResponse getSpotifyTrackData(String spotifyUrl) {
        URI spotifyUri = createUri(spotifyUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(SPOTIFY_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(spotifyUri, HttpMethod.GET, entity, SpotifyTrackResponse.class).getBody();
    }

    private URI getYoutubeApiUri(String songTitle) {
        String encodedMusicName = URLEncoder.encode(songTitle, StandardCharsets.UTF_8);
        String youtubeUrl = "https://youtube.googleapis.com/youtube/v3/search?fields=items(id(videoId))" +
                "&maxResults=1&q=" +
                encodedMusicName +
                "&key=" +
                YOUTUBE_API_KEY;

        return createUri(youtubeUrl);
    }

    private URI createUri(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }
}