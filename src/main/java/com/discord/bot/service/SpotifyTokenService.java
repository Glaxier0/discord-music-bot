package com.discord.bot.service;

import com.discord.bot.dto.response.spotify.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@Service
public class SpotifyTokenService {
    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    public void getAccessToken() {
        RestTemplate restTemplate = new RestTemplateBuilder().build();
        URI uri = createUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> bodyParamMap = new LinkedMultiValueMap<>();
        bodyParamMap.add("grant_type", "client_credentials");
        bodyParamMap.add("client_id", clientId);
        bodyParamMap.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(bodyParamMap, headers);
        TokenResponse tokenResponse = restTemplate.exchange(uri, HttpMethod.POST, entity, TokenResponse.class).getBody();
        assert tokenResponse != null;
        RestService.spotifyToken = tokenResponse.getAccessToken();
    }

    private URI createUri() {
        try {
            return new URI("https://accounts.spotify.com/api/token");
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + "https://accounts.spotify.com/api/token", e);
        }
    }
}