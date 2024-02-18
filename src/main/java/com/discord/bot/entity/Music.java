package com.discord.bot.entity;

import jakarta.persistence.*;
import lombok.*;

@SuppressWarnings("JpaDataSourceORMInspection")
@Table(name = "musics")
@Entity
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class Music {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String title;
    @Column(name = "youtube_uri")
    private String youtubeUri;
}