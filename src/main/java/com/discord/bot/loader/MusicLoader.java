package com.discord.bot.loader;

import com.discord.bot.entity.pojo.MusicPojo;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.List;

public interface MusicLoader {
    public List<MusicPojo> getMusicPojos(String query, MessageChannel channel);
}
