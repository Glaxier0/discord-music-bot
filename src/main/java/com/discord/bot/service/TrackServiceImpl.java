package com.discord.bot.service;

import com.discord.bot.dao.TrackRepository;
import com.discord.bot.entity.MusicData;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
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
    public MusicData findFirst1ByTitle(String title) {
        return trackRepository.findFirst1ByTitle(title);
    }

    @Override
    public void deleteAll() {
        trackRepository.deleteAll();
    }

    @Override
    public void cache(String title, String uri) {
        if (title != null) {
            MusicData musicData = new MusicData(title, uri);
            MusicData redisMusicData = findFirst1ByTitle(musicData.getTitle());
            if (redisMusicData == null) {
                save(musicData);
            }
        }
    }
}
