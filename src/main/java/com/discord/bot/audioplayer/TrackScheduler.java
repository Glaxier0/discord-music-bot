package com.discord.bot.audioplayer;

import com.discord.bot.entity.MusicData;
import com.discord.bot.service.TrackService;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {
    public final AudioPlayer player;
    public BlockingQueue<AudioTrack> queue;
    public boolean repeating = false;
    public SlashCommandInteractionEvent event;
    private int COUNT = 0;
    TrackService trackService;

    public TrackScheduler(AudioPlayer player, SlashCommandInteractionEvent event, TrackService trackService) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
        this.event = event;
        this.trackService = trackService;
    }

    public void setEvent(SlashCommandInteractionEvent event) {
        this.event = event;
    }

    public void queue(AudioTrack track) {
        if (!this.player.startTrack(track, true)) {
            this.queue.offer(track);
        }
        long start = System.currentTimeMillis();
        String title = event.getOption("query").getAsString();
        MusicData musicData = new MusicData(title, track.getInfo().uri);
        trackService.save(musicData);
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.println(timeElapsed);
    }

    public void nextTrack() {
        this.player.startTrack(this.queue.poll(), false);
        if (player.getPlayingTrack() == null) {
            event.getGuild().getAudioManager().closeAudioConnection();
        }
        if (repeating) {
            repeating = false;
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        event.getChannel().sendMessageEmbeds(new EmbedBuilder().setTitle("Now playing").setDescription("[" + track.getInfo().title
                + "](" + track.getInfo().uri + ")").setColor(Color.GREEN).build()).queue();
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        if (COUNT >= 1) {
            COUNT = 0;
            event.getChannel().sendMessageEmbeds(new EmbedBuilder()
                    .setDescription("Track failed to start.").build()).queue();
            return;
        }
        player.startTrack(track.makeClone(), false);
        COUNT++;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (event.getGuild().getSelfMember().getVoiceState().getChannel().getMembers().size() == 1) {
            event.getGuild().getAudioManager().closeAudioConnection();
            return;
        }
        if (endReason.mayStartNext) {
            if (this.repeating) {
                this.player.startTrack(track.makeClone(), false);
                return;
            }
            nextTrack();
        }
    }
}