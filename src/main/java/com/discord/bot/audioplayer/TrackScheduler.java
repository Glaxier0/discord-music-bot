package com.discord.bot.audioplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@SuppressWarnings("CanBeFinal")
public class TrackScheduler extends AudioEventAdapter {
    public final AudioPlayer player;
    public BlockingQueue<AudioTrack> queue;
    public boolean repeating = false;
    public SlashCommandInteractionEvent event;
    private int COUNT = 0;

    public TrackScheduler(AudioPlayer player, SlashCommandInteractionEvent event) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
        this.event = event;
    }

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void setEvent(SlashCommandInteractionEvent event) {
        this.event = event;
    }

    public void queue(AudioTrack track) {
        if (!this.player.startTrack(track, true)) {
            boolean offerSuccess = this.queue.offer(track);

            if (!offerSuccess) {
                System.err.println("Queue is full, could not add track: " + track.getInfo().title);
            }
        }
    }

    public void queueAll(List<AudioTrack> tracks) {
        for (AudioTrack track : tracks) {
            if (!this.player.startTrack(track, true)) {
                boolean offerSuccess = this.queue.offer(track);

                if (!offerSuccess)
                    System.out.println("Queue is full, could not add track and tracks after: " + track.getInfo().title);
            }
        }
    }

    public void nextTrack() {
        this.player.startTrack(this.queue.poll(), false);
        if (player.getPlayingTrack() == null) {
            if (event.getGuild() != null) {
                event.getGuild().getAudioManager().closeAudioConnection();
            }
        }
        if (repeating) {
            repeating = false;
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        if (COUNT >= 1) {
            COUNT = 0;
            event.getMessageChannel().sendMessageEmbeds(new EmbedBuilder()
                    .setDescription("Track failed to start.").build()).queue();
            return;
        }
        player.startTrack(track.makeClone(), false);
        COUNT++;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (event.getGuild() != null) {
            if (event.getGuild().getSelfMember().getVoiceState() != null
                    && event.getGuild().getSelfMember().getVoiceState().getChannel() != null) {
                if (event.getGuild().getSelfMember().getVoiceState().getChannel().getMembers().size() == 1) {
                    event.getGuild().getAudioManager().closeAudioConnection();
                    return;
                }
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
}