package com.discord.bot.dao;

import com.discord.bot.entity.MusicData;
import org.springframework.data.repository.CrudRepository;

public interface TrackRepository extends CrudRepository<MusicData, String> {
    MusicData findFirst1ByTitle(String title);
}
