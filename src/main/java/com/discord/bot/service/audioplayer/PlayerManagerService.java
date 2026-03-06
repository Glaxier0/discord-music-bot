package com.discord.bot.service.audioplayer;

import java.awt.Color;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.discord.bot.audioplayer.GuildMusicManager;
import com.discord.bot.dto.MultipleMusicDto;
import com.discord.bot.dto.MusicDto;
import com.discord.bot.service.ReplyService;

import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.LavalinkNode;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.event.*;
import dev.arbjerg.lavalink.client.player.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;

@Service
public class PlayerManagerService {
    private final Logger logger = LoggerFactory.getLogger(PlayerManagerService.class);
    private final Map<Long, GuildMusicManager> musicManagers = new HashMap<>();
    private final Map<String, List<Track>> pendingSoundCloudSelections = new ConcurrentHashMap<>();
    private final ReplyService replyService;
    private LavalinkClient lavalinkClient;

    @Value("${discord.bot.token}")
    private String discordToken;

    @Value("${lavalink.server.uri:ws://localhost:2333}")
    private String lavalinkUri;

    @Value("${lavalink.server.password:youshallnotpass}")
    private String lavalinkPassword;

    public PlayerManagerService(ReplyService replyService) {
        this.replyService = replyService;
    }

    @PostConstruct
    private void initLavalink() {
        long userId = Helpers.getUserIdFromToken(discordToken);
        this.lavalinkClient = new LavalinkClient(userId);

        lavalinkClient.addNode(
                new NodeOptions.Builder()
                        .setName("main")
                        .setServerUri(URI.create(lavalinkUri))
                        .setPassword(lavalinkPassword)
                        .build()
        );

        registerLavalinkListeners();
    }

    @PreDestroy
    private void shutdown() {
        if (lavalinkClient != null) {
            lavalinkClient.close();
        }
    }

    private void registerLavalinkListeners() {
        lavalinkClient.on(ReadyEvent.class).subscribe((event) -> {
            LavalinkNode node = event.getNode();
            logger.info("Lavalink node '{}' is ready, session id: '{}'",
                    node.getName(), event.getSessionId());
        });

        lavalinkClient.on(TrackStartEvent.class).subscribe((event) -> {
            Optional.ofNullable(musicManagers.get(event.getGuildId())).ifPresent(
                    (mng) -> mng.getScheduler().onTrackStart(event.getTrack())
            );
        });

        lavalinkClient.on(TrackEndEvent.class).subscribe((event) -> {
            Optional.ofNullable(musicManagers.get(event.getGuildId())).ifPresent(
                    (mng) -> mng.getScheduler().onTrackEnd(event.getTrack(), event.getEndReason())
            );
        });

        lavalinkClient.on(TrackExceptionEvent.class).subscribe((event) -> {
            Optional.ofNullable(musicManagers.get(event.getGuildId())).ifPresent(
                    (mng) -> mng.getScheduler().onTrackException(event.getTrack(),
                            event.getException().getMessage())
            );
        });

        lavalinkClient.on(TrackStuckEvent.class).subscribe((event) -> {
            Optional.ofNullable(musicManagers.get(event.getGuildId())).ifPresent(
                    (mng) -> mng.getScheduler().onTrackStuck(event.getTrack(), event.getThresholdMs())
            );
        });

        lavalinkClient.on(StatsEvent.class).subscribe((event) -> {
            logger.debug("Lavalink node '{}': players {}/{}", event.getNode().getName(),
                    event.getPlayingPlayers(), event.getPlayers());
        });
    }

    public LavalinkClient getLavalinkClient() {
        return lavalinkClient;
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        if (guild != null) {
            return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) ->
                    new GuildMusicManager(guildId, lavalinkClient)
            );
        }
        return null;
    }

    public void loadAndPlay(SlashCommandInteractionEvent event, MusicDto musicDto, boolean ephemeral) {
        final GuildMusicManager musicManager = this.getMusicManager(event.getGuild());
        final long guildId = event.getGuild().getIdLong();
        final Link link = lavalinkClient.getOrCreateLink(guildId);

        link.loadItem(musicDto.getYoutubeUri()).subscribe((loadResult) -> {
            if (loadResult instanceof TrackLoaded trackLoaded) {
                Track track = trackLoaded.getTrack();
                replyService.deferReply(event, "Song added to the queue: "
                                + track.getInfo().getTitle()
                                + "\n in queue: "
                                + (musicManager.getScheduler().queue.size() + 1),
                        Color.GREEN, ephemeral);
                musicManager.getScheduler().queue(track);

            } else if (loadResult instanceof PlaylistLoaded playlistLoaded) {
                List<Track> tracks = playlistLoaded.getTracks();
                if (tracks.isEmpty()) {
                    replyService.deferReply(event, "Playlist is empty.", Color.RED, ephemeral);
                    return;
                }
                for (Track track : tracks) {
                    musicManager.getScheduler().queue(track);
                }
                replyService.deferReply(event, tracks.size() + " songs added to the queue.",
                        Color.GREEN, ephemeral);

            } else if (loadResult instanceof SearchResult searchResult) {
                List<Track> tracks = searchResult.getTracks();
                if (tracks.isEmpty()) {
                    replyService.deferReply(event, "No matches found for: " + musicDto.getYoutubeUri(),
                            Color.RED, ephemeral);
                    return;
                }
                Track firstTrack = tracks.get(0);
                replyService.deferReply(event, "Song added to the queue: "
                                + firstTrack.getInfo().getTitle()
                                + "\n in queue: "
                                + (musicManager.getScheduler().queue.size() + 1),
                        Color.GREEN, ephemeral);
                musicManager.getScheduler().queue(firstTrack);

            } else if (loadResult instanceof NoMatches) {
                logger.warn("No match found for: {}", musicDto.getYoutubeUri());
                replyService.deferReply(event, "No matches found for: " + musicDto.getYoutubeUri(),
                        Color.RED, ephemeral);

            } else if (loadResult instanceof LoadFailed loadFailed) {
                logger.error("Track load failed: {}", loadFailed.getException().getMessage());
                replyService.deferReply(event, "Failed to load track: " + musicDto.getYoutubeUri(),
                        Color.RED, ephemeral);
            }
        }, (error) -> {
            logger.error("Error loading track", error);
            replyService.deferReply(event, "Failed to load track: " + musicDto.getYoutubeUri(),
                    Color.RED, ephemeral);
        });
    }

    @Async
    public void loadMultipleAndPlay(SlashCommandInteractionEvent event,
                                     MultipleMusicDto multipleMusicDto,
                                     boolean ephemeral) {
        GuildMusicManager musicManager = getMusicManager(event.getGuild());
        List<MusicDto> tracksToLoad = multipleMusicDto.getMusicDtoList();
        int totalTracks = tracksToLoad.size();
        final long guildId = event.getGuild().getIdLong();
        final Link link = lavalinkClient.getOrCreateLink(guildId);

        if (totalTracks == 0) {
            replyService.deferReply(event, "No tracks to load!", Color.RED, ephemeral);
            return;
        }

        EmbedBuilder loadingEmbed = new EmbedBuilder()
                .setDescription("Loading " + totalTracks + " track(s)...")
                .setColor(Color.YELLOW);

        event.getHook().sendMessageEmbeds(loadingEmbed.build())
                .setEphemeral(ephemeral)
                .queue(originalMessage -> {
                    AtomicInteger successCount = new AtomicInteger(0);
                    AtomicInteger failureCount = new AtomicInteger(0);
                    AtomicInteger completedCount = new AtomicInteger(0);

                    for (MusicDto musicDto : tracksToLoad) {
                        link.loadItem(musicDto.getYoutubeUri()).subscribe((loadResult) -> {
                            if (loadResult instanceof TrackLoaded trackLoaded) {
                                musicManager.getScheduler().queue(trackLoaded.getTrack());
                                successCount.incrementAndGet();

                            } else if (loadResult instanceof PlaylistLoaded playlistLoaded) {
                                List<Track> tracks = playlistLoaded.getTracks();
                                if (!tracks.isEmpty()) {
                                    for (Track track : tracks) {
                                        musicManager.getScheduler().queue(track);
                                    }
                                    successCount.addAndGet(tracks.size());
                                }

                            } else if (loadResult instanceof SearchResult searchResult) {
                                List<Track> tracks = searchResult.getTracks();
                                if (!tracks.isEmpty()) {
                                    musicManager.getScheduler().queue(tracks.get(0));
                                    successCount.incrementAndGet();
                                } else {
                                    failureCount.incrementAndGet();
                                }

                            } else if (loadResult instanceof NoMatches) {
                                logFailure(musicDto.getYoutubeUri(), "No matches found");
                                failureCount.incrementAndGet();

                            } else if (loadResult instanceof LoadFailed loadFailed) {
                                logFailure(musicDto.getYoutubeUri(), "Load failed: "
                                        + loadFailed.getException().getMessage());
                                failureCount.incrementAndGet();
                            }

                            checkAndSendResult(completedCount, totalTracks, successCount,
                                    failureCount, originalMessage);
                        }, (error) -> {
                            logFailure(musicDto.getYoutubeUri(), "Error", (Exception) error);
                            failureCount.incrementAndGet();
                            checkAndSendResult(completedCount, totalTracks, successCount,
                                    failureCount, originalMessage);
                        });
                    }
                });
    }

    private void checkAndSendResult(AtomicInteger completedCount, int totalTracks,
                                     AtomicInteger successCount, AtomicInteger failureCount,
                                     net.dv8tion.jda.api.entities.Message originalMessage) {
        if (completedCount.incrementAndGet() == totalTracks) {
            int succeeded = successCount.get();
            int failed = failureCount.get();

            EmbedBuilder resultEmbed = new EmbedBuilder()
                    .setColor(failed > 0 ? (failed == totalTracks ? Color.RED : Color.ORANGE) : Color.GREEN)
                    .setDescription(createResultMessage(succeeded, failed, totalTracks));

            originalMessage.editMessageEmbeds(resultEmbed.build()).queue();
        }
    }

    private String createResultMessage(int succeeded, int failed, int total) {
        if (failed == 0) {
            return String.format("Successfully queued all %d tracks!", total);
        } else if (succeeded == 0) {
            return String.format("Failed to load all %d tracks!", total);
        } else {
            return String.format("%d track(s) queued\n%d failed (of %d total)",
                    succeeded, failed, total);
        }
    }

    private void logFailure(String uri, String message) {
        logger.warn("Failed to load {}: {}", uri, message);
    }

    private void logFailure(String uri, String message, Exception ex) {
        logger.error("Failed to load {}: {}", uri, message, ex);
    }

    public void storePendingSelection(String key, List<Track> tracks) {
        pendingSoundCloudSelections.put(key, tracks);
    }

    public List<Track> getPendingSelection(String key) {
        return pendingSoundCloudSelections.get(key);
    }

    public void removePendingSelection(String key) {
        pendingSoundCloudSelections.remove(key);
    }

    public void searchAndShowResults(SlashCommandInteractionEvent event, MusicDto musicDto, boolean ephemeral) {
        final long guildId = event.getGuild().getIdLong();
        final Link link = lavalinkClient.getOrCreateLink(guildId);

        link.loadItem(musicDto.getYoutubeUri()).subscribe((loadResult) -> {
            if (loadResult instanceof SearchResult searchResult) {
                List<Track> tracks = searchResult.getTracks();
                if (tracks.isEmpty()) {
                    replyService.deferReply(event, "No matches found on SoundCloud for: " + musicDto.getTitle(),
                            Color.RED, ephemeral);
                    return;
                }

                List<Track> topTracks = tracks.stream().limit(5).toList();
                String key = guildId + "_" + event.getUser().getId();
                storePendingSelection(key, topTracks);

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("SoundCloud Search Results")
                        .setDescription("Select a track to play:\n")
                        .setColor(Color.ORANGE);

                List<Button> buttons = new ArrayList<>();
                for (int i = 0; i < topTracks.size(); i++) {
                    var info = topTracks.get(i).getInfo();
                    embed.appendDescription((i + 1) + ". " + info.getTitle() + " - " + info.getAuthor() + "\n");
                    buttons.add(Button.primary("sc_" + i, String.valueOf(i + 1)));
                }

                event.getHook().sendMessageEmbeds(embed.build())
                        .setComponents(ActionRow.of(buttons))
                        .setEphemeral(ephemeral)
                        .queue();

            } else if (loadResult instanceof NoMatches) {
                replyService.deferReply(event, "No matches found on SoundCloud for: " + musicDto.getTitle(),
                        Color.RED, ephemeral);

            } else if (loadResult instanceof LoadFailed loadFailed) {
                logger.error("SoundCloud search failed: {}", loadFailed.getException().getMessage());
                replyService.deferReply(event, "Failed to search SoundCloud: "
                        + loadFailed.getException().getMessage(), Color.RED, ephemeral);
            }
        }, (error) -> {
            logger.error("Error searching SoundCloud", error);
            replyService.deferReply(event, "Failed to search SoundCloud.", Color.RED, ephemeral);
        });
    }
}
