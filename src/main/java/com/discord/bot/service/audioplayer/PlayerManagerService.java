package com.discord.bot.service.audioplayer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.TvHtml5EmbeddedWithThumbnail;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import dev.lavalink.youtube.clients.skeleton.Client;

@Service
public class PlayerManagerService {
    private final static Logger logger = LoggerFactory.getLogger(PlayerManagerService.class);
    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;
    private final ReplyService replyService;

    @Value("${youtube.refreshToken}")
    private String refreshToken;

    public PlayerManagerService(ReplyService replyService) {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        this.replyService = replyService;
    }

    @PostConstruct
    private void SetYoutubeToken() {
        YoutubeAudioSourceManager sourceManager = new YoutubeAudioSourceManager(
                true,
                new Client[] {
                        new TvHtml5EmbeddedWithThumbnail()
                });
        sourceManager.useOauth2(refreshToken, true);
        audioPlayerManager.registerSourceManager(sourceManager);
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        if (guild != null) {
            return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
                final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager, guild);

                guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());

                return guildMusicManager;
            });
        }

        return null;
    }

    public void loadAndPlay(SlashCommandInteractionEvent event, MusicDto musicDto, boolean ephemeral) {
        final GuildMusicManager musicManager = this.getMusicManager(event.getGuild());
        this.audioPlayerManager.loadItemOrdered(musicManager, musicDto.getYoutubeUri(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                replyService.reply(event, "Song added to the queue: "
                        + track.getInfo().title
                        + "\n in queue: "
                        + (musicManager.scheduler.queue.size() + 1),
                    Color.GREEN, ephemeral);
                musicManager.scheduler.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                List<AudioTrack> tracks = playlist.getTracks();
                if (playlist.isSearchResult() && !playlist.getTracks().isEmpty()) {
                    AudioTrack firstResult = playlist.getTracks().get(0);
                    replyService.reply(event, "Song added to the queue: "
                            + firstResult.getInfo().title
                            + "\n in queue: "
                            + (musicManager.scheduler.queue.size() + 1),
                        Color.GREEN, ephemeral);
                    musicManager.scheduler.queue(firstResult);
                } else {
                    for (AudioTrack track : tracks) {
                        musicManager.scheduler.queue(track);
                    }
                    replyService.reply(event, tracks.size() + " songs added to the queue.", Color.GREEN, ephemeral);
                }
            }

            @Override
            public void noMatches() {
                logger.warn("No match is found for: {}", musicDto.getYoutubeUri());
                replyService.reply(event, "No matches found for: " + musicDto.getYoutubeUri(), Color.RED, ephemeral);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                logger.error("Track load failed.", exception);
                replyService.reply(event, "Failed to load track: " + musicDto.getYoutubeUri(), Color.RED, ephemeral);
            }
        });
    }

    @Async
    public void loadMultipleAndPlay(SlashCommandInteractionEvent event,
            MultipleMusicDto multipleMusicDto,
            boolean ephemeral) {
        GuildMusicManager musicManager = getMusicManager(event.getGuild());
        List<MusicDto> tracksToLoad = multipleMusicDto.getMusicDtoList();
        int totalTracks = tracksToLoad.size();

        if (totalTracks == 0) {
            replyService.reply(event, "⚠ No tracks to load!", Color.RED, ephemeral);
            return;
        }

        EmbedBuilder loadingEmbed = new EmbedBuilder()
                .setDescription("⏳ Loading " + totalTracks + " track(s)...")
                .setColor(Color.YELLOW);

        event.getHook().sendMessageEmbeds(loadingEmbed.build())
                .setEphemeral(ephemeral)
                .queue(originalMessage -> {
                    AtomicInteger successCount = new AtomicInteger(0);
                    AtomicInteger failureCount = new AtomicInteger(0);
                    List<CompletableFuture<Void>> futures = new ArrayList<>(totalTracks);

                    for (MusicDto musicDto : tracksToLoad) {
                        CompletableFuture<Void> cf = new CompletableFuture<>();
                        futures.add(cf);

                        audioPlayerManager.loadItemOrdered(musicManager, musicDto.getYoutubeUri(),
                                new AudioLoadResultHandler() {
                                    @Override
                                    public void trackLoaded(AudioTrack track) {
                                        queueTrack(musicManager, track);
                                        successCount.incrementAndGet();
                                        cf.complete(null);
                                    }

                                    @Override
                                    public void playlistLoaded(AudioPlaylist playlist) {
                                        int added = handlePlaylist(musicManager, playlist);
                                        successCount.addAndGet(added);
                                        cf.complete(null);
                                    }

                                    @Override
                                    public void noMatches() {
                                        logFailure(musicDto.getYoutubeUri(), "No matches found");
                                        failureCount.incrementAndGet();
                                        cf.complete(null);
                                    }

                                    @Override
                                    public void loadFailed(FriendlyException exception) {
                                        logFailure(musicDto.getYoutubeUri(), "Load failed", exception);
                                        failureCount.incrementAndGet();
                                        cf.complete(null);
                                    }
                                });
                    }

                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                            .thenRun(() -> {
                                int succeeded = successCount.get();
                                int failed = failureCount.get();

                                EmbedBuilder resultEmbed = new EmbedBuilder()
                                        .setColor(failed > 0 ? (failed == totalTracks ? Color.RED : Color.ORANGE)
                                                : Color.GREEN)
                                        .setDescription(createResultMessage(succeeded, failed, totalTracks));

                                originalMessage.editMessageEmbeds(resultEmbed.build()).queue();
                            });
                });
    }

    private int handlePlaylist(GuildMusicManager manager, AudioPlaylist playlist) {
        if (playlist.isSearchResult() && !playlist.getTracks().isEmpty()) {
            queueTrack(manager, playlist.getTracks().get(0));
            return 1;
        }

        playlist.getTracks().forEach(track -> queueTrack(manager, track));
        return playlist.getTracks().size();
    }

    private void queueTrack(GuildMusicManager manager, AudioTrack track) {
        manager.scheduler.queue(track);
    }

    private String createResultMessage(int succeeded, int failed, int total) {
        if (failed == 0) {
            return String.format("✅ Successfully queued all %d tracks!", total);
        } else if (succeeded == 0) {
            return String.format("❌ Failed to load all %d tracks!", total);
        } else {
            return String.format("✅ %d track(s) queued\n❌ %d failed (of %d total)",
                    succeeded, failed, total);
        }
    }

    private void logFailure(String uri, String message) {
        logger.warn("Failed to load {}: {}", uri, message);
    }

    private void logFailure(String uri, String message, Exception ex) {
        logger.error("Failed to load {}: {}", uri, message, ex);
    }
}