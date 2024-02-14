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
    private int count;
    private List<MusicDto> musicDtoList;
    private int failCount;
}