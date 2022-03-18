package com.discord.bot.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash("music")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class MusicData {
    @Id
    String id;
    @Indexed
    String title;
    String youtubeUri;

    public MusicData(String title, String youtubeUri) {
        this.title = title;
        this.youtubeUri = youtubeUri;
    }
}
