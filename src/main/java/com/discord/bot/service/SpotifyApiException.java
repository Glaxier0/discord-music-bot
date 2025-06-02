package com.discord.bot.service;

public class SpotifyApiException extends RuntimeException {
    public SpotifyApiException(String message) {
        super(message);
    }
}