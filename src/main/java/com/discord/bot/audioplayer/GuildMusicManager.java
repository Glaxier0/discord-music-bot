package com.discord.bot.audioplayer;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Getter
@Setter
public class GuildMusicManager {
    private static final Logger logger = LoggerFactory.getLogger(GuildMusicManager.class);
    public final TrackScheduler scheduler;
    private final long guildId;
    private final LavalinkClient lavalinkClient;

    public GuildMusicManager(long guildId, LavalinkClient lavalinkClient) {
        this.guildId = guildId;
        this.lavalinkClient = lavalinkClient;
        this.scheduler = new TrackScheduler(this);
    }

    public void stop() {
        this.scheduler.queue.clear();
        this.scheduler.repeating = false;

        var link = getOrCreateLink();
        link.createOrUpdatePlayer()
                .setTrack(null)
                .subscribe(
                        (player) -> logger.debug("Player stopped for guild {}", guildId),
                        (error) -> logger.error("Failed to stop player for guild {}", guildId, error)
                );
    }

    public Optional<Link> getLink() {
        return Optional.ofNullable(
                this.lavalinkClient.getLinkIfCached(this.guildId)
        );
    }

    public Link getOrCreateLink() {
        return this.lavalinkClient.getOrCreateLink(this.guildId);
    }

    public Optional<LavalinkPlayer> getPlayer() {
        return this.getLink().map(Link::getCachedPlayer);
    }
}