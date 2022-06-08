package com.discord.bot.audioplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;

import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class TrackSchedulerTest {
    @InjectMocks
     TrackScheduler scheduler;
    
    @Mock
    AudioTrack track;
    @Mock
    AudioTrackInfo audioTrackInfo;
    @Mock
    AudioPlayer player;
    @Mock
    Guild guild;
    @Mock
    AudioManager audioManager;
    @Mock
    SlashCommandInteractionEvent event;
    @Mock
    ReplyCallbackAction replyCallbackAction;
    @Mock
    MessageChannel messageChannel;
    @Mock
    MessageAction messageAction;
    @Mock
    FriendlyException friendlyException;
    @Mock
    Member selfMember;
    @Mock
    GuildVoiceState guildVoiceState;
    @Mock
    AudioChannel audioChannel;
    
    List<Member> members = new ArrayList<>();
    
    @Before
    public void setUp() {}
    
    @BeforeEach
    public void eachSetUp() {
        scheduler = new TrackScheduler(player, event);
    }
    
    @AfterEach
    public void resetMocks() {
        reset(player);
        reset(event);
        reset(track);
        reset(guild);
        reset(audioManager);
    }
    
    /**
     * Purpose: Queueing new track in empty queue when there is currently playing track
     * Input: queue track
     * Expected:
     *      queue.size() == 1
     */
    @Test
    public void testQueueWhenPlayingNow() {
        when(player.startTrack(any(), eq(true))).thenReturn(false);
        scheduler.queue(track);
        assertEquals(1, scheduler.queue.size());
    }
    
    /**
     * Purpose: Queueing new track in empty queue when there is NO currently playing track
     * Input: queue track
     * Expected:
     *      queue.size() == 0
     */
    @Test
    public void testQueueWhenNotPlayingNow() {
        scheduler.setEvent(event);
        when(player.startTrack(any(), eq(true))).thenReturn(true);
        scheduler.queue(track);
        assertEquals(0, scheduler.queue.size());
    }

    /**
     * Purpose: Playing next track when there is next track to play
     * Input: nextTrack
     * Expected:
     *      audioConnection is not closed
     *      repeating is false
     */
    @Test
    public void testNextTrackWhenNextTrackExists() {
        scheduler.queue(track);
        scheduler.repeating = false;
        when(player.startTrack(any(),eq(false))).thenReturn(true);
        when(player.getPlayingTrack()).thenReturn(scheduler.queue.poll());
        scheduler.nextTrack();
        verify(audioManager, never()).closeAudioConnection();
        assertFalse(scheduler.repeating);
    }
    
    /**
     * Purpose: Playing next track when there is NO next track to play
     * Input: nextTrack
     * Expected:
     *      audioConnection is closed
     *      repeating is false
     */
    @Test
    public void testNextTrackWhenNextTrackNotExists() {
        scheduler.repeating = true;
        when(event.getGuild()).thenReturn(guild);
        when(guild.getAudioManager()).thenReturn(audioManager);
        doNothing().when(audioManager).closeAudioConnection();
        when(player.startTrack(any(),eq(false))).thenReturn(true);
        when(player.getPlayingTrack()).thenReturn(scheduler.queue.poll());
        
        scheduler.nextTrack();
        verify(audioManager).closeAudioConnection();
        assertFalse(scheduler.repeating);
    }

    /**
     * Purpose: Trigger callback when new track started
     * Input: onTrackStart player, track
     * Expected:
     *      messageEmbed will be sent to channel
     */
    @Test
    public void testOnTrackStart() {
        when(event.getChannel()).thenReturn(messageChannel);
        when(track.getInfo()).thenReturn(audioTrackInfo);
        when(messageChannel.sendMessageEmbeds(any(MessageEmbed.class))).thenReturn(messageAction);
        doNothing().when(messageAction).queue();
        scheduler.onTrackStart(player, track);
        verify(messageChannel).sendMessageEmbeds(any(MessageEmbed.class));
    }

    /**
     * Purpose: Trigger callback when exception occurred multiple time
     * Input: onTrackException player, track, exception
     * Expected:
     *      messageEmbed will be sent to channel
     *      replay current track
     */
    @Test
    public void testOnTrackException() {
        when(track.makeClone()).thenReturn(track);
        when(event.getChannel()).thenReturn(messageChannel);
        when(messageChannel.sendMessageEmbeds(any(MessageEmbed.class))).thenReturn(messageAction);
        doNothing().when(messageAction).queue();
        when(player.startTrack(any(),eq(false))).thenReturn(true);
        
        scheduler.onTrackException(player, track, friendlyException);
        verify(player).startTrack(any(),eq(false));
        scheduler.onTrackException(player, track, friendlyException);
        verify(player).startTrack(any(),eq(false));
        verify(messageChannel).sendMessageEmbeds(any(MessageEmbed.class));
    }

    /**
     * Purpose: Trigger callback when track ends, user still exists in channel and loop set
     * Input: onTrackEnd player, track, AudioTrackEndReason.FINISHED
     * Expected:
     *      audioConnection should not be closed
     *      replay current track
     */
    @Test
    public void testOnTrackEndWhenUserExistAndLooped() {
        when(event.getGuild()).thenReturn(guild);
        when(guild.getSelfMember()).thenReturn(selfMember);
        when(selfMember.getVoiceState()).thenReturn(guildVoiceState);
        when(guildVoiceState.getChannel()).thenReturn(audioChannel);
        members.add(selfMember);
        members.add(selfMember);
        when(track.makeClone()).thenReturn(track);
        when(player.startTrack(any(),eq(false))).thenReturn(true);
        scheduler.repeating = true;
        
        scheduler.onTrackEnd(player, track, AudioTrackEndReason.FINISHED);
        verify(audioManager, never()).closeAudioConnection();
        verify(player).startTrack(track,false);
    }

    /**
     * Purpose: Trigger callback when track ends, user still exists in channel and loop wasn't set
     * Input: onTrackEnd player, track, AudioTrackEndReason.FINISHED
     * Expected:
     *      audioConnection should not be closed
     *      play next track
     */
    @Test
    public void testOnTrackEndWhenUserExistAndNotLooped() {
        when(event.getGuild()).thenReturn(guild);
        when(guild.getSelfMember()).thenReturn(selfMember);
        when(selfMember.getVoiceState()).thenReturn(guildVoiceState);
        when(guildVoiceState.getChannel()).thenReturn(audioChannel);
        members.add(selfMember);
        members.add(selfMember);
        when(player.getPlayingTrack()).thenReturn(track);
        scheduler.repeating = false;

        scheduler.onTrackEnd(player, track, AudioTrackEndReason.FINISHED);
        verify(audioManager, never()).closeAudioConnection();
        verify(player, never()).startTrack(track,false);
    }

    /**
     * Purpose: Trigger callback when track ends, user doesn't exist in channel
     * Input: onTrackEnd player, track, AudioTrackEndReason.FINISHED
     * Expected:
     *      audioConnection should be closed
     */
    @Test
    public void testOnTrackEndWhenUserNotExist() {
        when(event.getGuild()).thenReturn(guild);
        when(guild.getSelfMember()).thenReturn(selfMember);
        when(guild.getAudioManager()).thenReturn(audioManager);
        when(selfMember.getVoiceState()).thenReturn(guildVoiceState);
        when(guildVoiceState.getChannel()).thenReturn(audioChannel);
        members.add(selfMember);
        when(audioChannel.getMembers()).thenReturn(members);
        doNothing().when(audioManager).closeAudioConnection();
        scheduler.repeating = false;

        scheduler.onTrackEnd(player, track, AudioTrackEndReason.FINISHED);
        verify(audioManager).closeAudioConnection();
    }

    /**
     * Purpose: Trigger callback when track ends, user still exists in channel and next track already played
     * Input: onTrackEnd player, track, AudioTrackEndReason.REPLACED
     * Expected:
     *     audioConnection should not be closed
     */
    @Test
    public void testOnTrackEndWhenNextTrackAlreadyPlayed() {
        when(event.getGuild()).thenReturn(guild);
        when(guild.getSelfMember()).thenReturn(selfMember);
        when(selfMember.getVoiceState()).thenReturn(guildVoiceState);
        when(guildVoiceState.getChannel()).thenReturn(audioChannel);
        members.add(selfMember);
        members.add(selfMember);

        scheduler.onTrackEnd(player, track, AudioTrackEndReason.REPLACED);
        verify(audioManager, never()).closeAudioConnection();
    }
}