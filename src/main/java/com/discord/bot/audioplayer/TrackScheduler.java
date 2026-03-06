package com.discord.bot.audioplayer;

import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TrackScheduler {
    private final static Logger logger = LoggerFactory.getLogger(TrackScheduler.class);
    private final GuildMusicManager guildMusicManager;
    public final Queue<Track> queue = new LinkedList<>();
    public boolean repeating = false;

    public TrackScheduler(GuildMusicManager guildMusicManager) {
        this.guildMusicManager = guildMusicManager;
    }

    public void queue(Track track) {
        logger.debug("Queueing track: {}", track.getInfo().getTitle());
        this.guildMusicManager.getPlayer().ifPresentOrElse(
                (player) -> {
                    if (player.getTrack() == null) {
                        logger.debug("No track currently playing, starting immediately.");
                        this.startTrack(track);
                    } else {
                        logger.debug("Track already playing, adding to queue. Queue size: {}", queue.size() + 1);
                        this.queue.offer(track);
                    }
                },
                () -> {
                    logger.debug("No cached player found, starting track directly.");
                    this.startTrack(track);
                }
        );
    }

    public void queueAll(List<Track> tracks) {
        if (tracks.isEmpty()) return;

        this.guildMusicManager.getPlayer().ifPresentOrElse(
                (player) -> {
                    if (player.getTrack() == null) {
                        this.startTrack(tracks.get(0));
                        for (int i = 1; i < tracks.size(); i++) {
                            this.queue.offer(tracks.get(i));
                        }
                    } else {
                        this.queue.addAll(tracks);
                    }
                },
                () -> {
                    this.startTrack(tracks.get(0));
                    for (int i = 1; i < tracks.size(); i++) {
                        this.queue.offer(tracks.get(i));
                    }
                }
        );
    }

    public void nextTrack() {
        Track nextTrack = this.queue.poll();
        if (nextTrack != null) {
            logger.debug("Playing next track: {}", nextTrack.getInfo().getTitle());
            this.startTrack(nextTrack);
        } else {
            logger.debug("Queue empty, stopping player.");
            // No more tracks, stop the player
            var link = this.guildMusicManager.getOrCreateLink();
            link.createOrUpdatePlayer()
                    .setTrack(null)
                    .subscribe(
                            (player) -> logger.debug("Player stopped successfully."),
                            (error) -> logger.error("Failed to stop player", error)
                    );
        }
    }

    public void onTrackStart(Track track) {
        logger.debug("Track started: {}", track.getInfo().getTitle());
    }

    public void onTrackEnd(Track lastTrack, Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason endReason) {
        if (endReason.getMayStartNext()) {
            if (this.repeating) {
                this.startTrack(lastTrack.makeClone());
                return;
            }
            nextTrack();
        }
    }

    public void onTrackException(Track track, String message) {
        logger.error("Track exception for {}: {}", track.getInfo().getTitle(), message);
        nextTrack();
    }

    public void onTrackStuck(Track track, long thresholdMs) {
        logger.error("Track stuck for {}: threshold {}ms", track.getInfo().getTitle(), thresholdMs);
        nextTrack();
    }

    private void startTrack(Track track) {
        logger.info("Attempting to start track: {}", track.getInfo().getTitle());
        var link = this.guildMusicManager.getOrCreateLink();
        link.createOrUpdatePlayer()
                .setTrack(track)
                .setVolume(100)
                .subscribe(
                        (player) -> logger.info("Track started successfully: {}", track.getInfo().getTitle()),
                        (error) -> logger.error("Failed to start track: {}", track.getInfo().getTitle(), error)
                );
    }
}