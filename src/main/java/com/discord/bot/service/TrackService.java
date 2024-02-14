package com.discord.bot.service;

import com.discord.bot.entity.MusicData;

import java.util.Optional;

public interface TrackService {
    void save(MusicData musicData);
    void delete(MusicData musicData);
    Optional<MusicData> findById(String id);
    MusicData findFirstByTitle(String title);
    void deleteAll();
}
