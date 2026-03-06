package com.discord.bot.service;

public class InvalidSpotifyUrlException extends RuntimeException {
    public InvalidSpotifyUrlException(String message) {
        super(message);
    }
}