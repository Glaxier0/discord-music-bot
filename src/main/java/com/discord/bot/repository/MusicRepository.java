package com.discord.bot.repository;

import com.discord.bot.entity.Music;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MusicRepository extends JpaRepository<Music, String> {
    Music findFirstByTitle(String title);
}
