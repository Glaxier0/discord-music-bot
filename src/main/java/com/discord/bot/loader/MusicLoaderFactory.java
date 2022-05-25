package com.discord.bot.loader;

import com.discord.bot.entity.pojo.MusicPojo;

public class MusicLoaderFactory {
    public static MusicLoader createMusicLoader(String query) {
        MusicLoader musicLoader = null;
        final boolean isYoutubeUrl = query.contains("https://www.youtube.com/watch?v=");
        final boolean isSpotifyUrl = query.contains("https://open.spotify.com/");

        if (isYoutubeUrl) {
            musicLoader = new YoutubeMusicLoader();
        } else if (isSpotifyUrl) {
            musicLoader = new SpotifyMusicLoader();
        } else {
            musicLoader = new YoutubeMusicLoader();
        }

        return musicLoader;
    }
}
