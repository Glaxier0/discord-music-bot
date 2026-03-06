package com.discord.bot.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class SearchSourceManager {
    private final AtomicBoolean youtubeSearchEnabled = new AtomicBoolean(true);

    public boolean isYoutubeSearchEnabled() {
        return youtubeSearchEnabled.get();
    }

    public void setYoutubeSearchEnabled(boolean enabled) {
        youtubeSearchEnabled.set(enabled);
    }
}
