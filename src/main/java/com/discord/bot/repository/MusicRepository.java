package com.discord.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.discord.bot.entity.Music;

public interface MusicRepository extends JpaRepository<Music, Integer> {
    Music findFirstByTitle(String title);
}
