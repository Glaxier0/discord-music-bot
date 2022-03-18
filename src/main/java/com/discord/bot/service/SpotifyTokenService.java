package com.discord.bot.service;

import com.google.gson.JsonParser;
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
    @Value("${spotify_client_id}")
    private String CLIENT_ID ;
    @Value("${spotify_client_secret}")
    private String CLIENT_SECRET;
    private final String TOKEN_URL = "https://accounts.spotify.com/api/token";

    public void getAccessToken() {
        RestTemplate restTemplate = new RestTemplateBuilder().build();
        URI uri = null;
        try {
            uri = new URI(TOKEN_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> bodyParamMap = new LinkedMultiValueMap<>();
        bodyParamMap.add("grant_type", "client_credentials");
        bodyParamMap.add("client_id", CLIENT_ID);
        bodyParamMap.add("client_secret", CLIENT_SECRET);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(bodyParamMap, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
        String accessToken = new JsonParser().parse(responseEntity.getBody()).getAsJsonObject().get("access_token").getAsString();
        RestService.SPOTIFY_TOKEN = accessToken;
    }
}