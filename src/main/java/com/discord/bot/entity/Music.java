package com.discord.bot.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Table(name = "musics")
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@SuppressWarnings("JpaDataSourceORMInspection")
public class Music {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    String id;
    String title;
    @Column(name = "youtube_uri")
    String youtubeUri;

    public Music(String title, String youtubeUri) {
        this.title = title;
        this.youtubeUri = youtubeUri;
    }
}