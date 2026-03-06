package com.discord.bot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class MultipleMusicDto {
    private List<MusicDto> musicDtoList;
    private String errorMessage;

    public MultipleMusicDto(List<MusicDto> musicDtoList) {
        this.musicDtoList = musicDtoList;
        this.errorMessage = null;
    }

    public MultipleMusicDto(String errorMessage) {
        this.musicDtoList = List.of();
        this.errorMessage = errorMessage;
    }

    public boolean hasError() {
        return errorMessage != null;
    }

    public static MultipleMusicDto error(String message) {
        return new MultipleMusicDto(message);
    }

    public static MultipleMusicDto of(List<MusicDto> dtos) {
        return new MultipleMusicDto(dtos);
    }
}