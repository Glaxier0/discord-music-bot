package com.discord.bot.dto.response.youtube;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class YoutubeResponse {
    private List<ItemDto> items;
}