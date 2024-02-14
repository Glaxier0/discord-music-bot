package com.discord.bot.service;

import com.discord.bot.dao.TrackRepository;
import com.discord.bot.entity.MusicData;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class TrackServiceImpl implements TrackService {
    TrackRepository trackRepository;

    @Override
    public void save(MusicData musicData) {
        trackRepository.save(musicData);
    }

    @Override
    public void delete(MusicData musicData) {
        trackRepository.delete(musicData);
    }

    @Override
    public Optional<MusicData> findById(String id) {
        return trackRepository.findById(id);
    }

    @Override
    public MusicData findFirstByTitle(String title) {
        return trackRepository.findFirstByTitle(title);
    }

    @Override
    public void deleteAll() {
        trackRepository.deleteAll();
    }
}
