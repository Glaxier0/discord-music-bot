package com.discord.bot.service;

import com.discord.bot.BotApplication;
import com.discord.bot.dao.TrackRepository;
import com.discord.bot.entity.pojo.MusicPojo;
import com.discord.bot.loader.*;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.internal.matchers.Any;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;

@RunWith(PowerMockRunner.class)
public class MusicLoaderTest {
    @Mock
    RestService restService = mock(RestService.class);

    String youtubeSingleMusicUrl = "https://www.youtube.com/watch?v=em0MknB6wFo";
    String youtubePlaylistUrl = "https://www.youtube.com/watch?v=A2VpR8HahKc&list=PLSdoVPM5WnndSQEXRz704yQkKwx76GvPV";
    String youtubeErrorUrl = "https://www.youtube.com/watch?v=123";
    String spotifyTrackUrl = "https://open.spotify.com/track/2Foc5Q5nqNiosCNqttzHof";
    String spotifyPlaylistUrl = "https://open.spotify.com/playlist/4mVGPbV9WiSoogLFfZC7zn";
    String musicName = "viva la vida";
    String musicNameUrl = "https://www.youtube.com/watch?v=HosW0gulISQ";


    /**
     * Purpose: Verify the correct MusicLoader is generated for the given URL
     * Input: URLs to MusicLoaderFactory
     *  String(Youtube Single Track)
     *  String(Youtube Playlist)
     *  String(Spotify Single Track)
     *  String(Spotify Playlist)
     *  String(Music Name)
     * Expected:
     *  YoutubeMusicLoader Object
     *  YoutubeMusicLoader Object
     *  SpotifyMusicLoader Object
     *  SpotifyMusicLoader Object
     *  YoutubeMusicLoader Object
     */
    @Test
    public void MusicLoaderFactoryTest() {
        MusicLoader youtubeSingleLoader = MusicLoaderFactory.createMusicLoader(youtubeSingleMusicUrl);
        MusicLoader youtubeListLoader = MusicLoaderFactory.createMusicLoader(youtubePlaylistUrl);
        MusicLoader spotifySingleLoader = MusicLoaderFactory.createMusicLoader(spotifyTrackUrl);
        MusicLoader spotifyListLoader = MusicLoaderFactory.createMusicLoader(spotifyPlaylistUrl);
        MusicLoader musicNameLoader = MusicLoaderFactory.createMusicLoader(musicName);
        assertTrue(youtubeSingleLoader instanceof YoutubeMusicLoader);
        assertTrue(youtubeListLoader instanceof YoutubeMusicLoader);
        assertTrue(spotifySingleLoader instanceof SpotifyMusicLoader);
        assertTrue(spotifyListLoader instanceof SpotifyMusicLoader);
        assertTrue(musicNameLoader instanceof YoutubeMusicLoader);
    }

    /**
     * Purpose: Verify YoutubeMusicLoader class generates a list when give Single Youtube Track Url
     * Input: URLs to YoutubeMusicLoader
     *  String(Youtube Single Track Url)
     * Expected:
     *  size 1, List<MusicPojo>, first element in this list has same String as Youtube Single Track URL at youtubeUri field
     */
    @Test
    public void YoutubeMusicLoaderSingleLoadTest() {
        when(restService.getYoutubeLink(ArgumentMatchers.<MusicPojo>any())).thenReturn(new MusicPojo("", youtubePlaylistUrl));
        MusicLoader youtubeSingleLoader = MusicLoaderFactory.createMusicLoader(youtubeSingleMusicUrl);
        List<MusicPojo> musicPojos = youtubeSingleLoader.getMusicPojos(restService, youtubeSingleMusicUrl, null);

        assertEquals(1, musicPojos.size());
        assertEquals(youtubeSingleMusicUrl, musicPojos.get(0).getYoutubeUri());
    }

    /**
     * Purpose: Verify YoutubeMusicLoader class generates a list when give Single Youtube Playlist Url
     * Input: URLs to YoutubeMusicLoader
     *  String(Youtube Playlist Url)
     * Expected:
     *  size 1, List<MusicPojo>, first element in this list has same String as Youtube Playlist URL at youtubeUri field
     */
    @Test
    public void YoutubeMusicLoaderPlaylistLoadTest() {
        when(restService.getYoutubeLink(any(MusicPojo.class))).thenReturn(new MusicPojo("", youtubePlaylistUrl));

        MusicLoader youtubePlaylistLoader = MusicLoaderFactory.createMusicLoader(youtubePlaylistUrl);
        List<MusicPojo> musicPojos = youtubePlaylistLoader.getMusicPojos(restService, youtubePlaylistUrl, null);

        assertEquals(1, musicPojos.size());
        assertEquals(youtubePlaylistUrl, musicPojos.get(0).getYoutubeUri());
    }

    /**
     * Purpose: Verify YoutubeMusicLoader class generates a list when give MusicName
     * Input: MusicName to YoutubeMusicLoader
     *  String(Music Name)
     * Expected:
     *  size 1, List<MusicPojo>, first element in this list has same String as musicNameUrl at youtubeUri field
     */
    @Test
    public void YoutubeMusicLoaderMusicNameLoadTest() {
        when(restService.getYoutubeLink(any(MusicPojo.class))).thenReturn(new MusicPojo("", musicNameUrl));

        MusicLoader youtubeMusicNameLoader = MusicLoaderFactory.createMusicLoader(musicName);
        List<MusicPojo> musicPojos = youtubeMusicNameLoader.getMusicPojos(restService, musicName, null);

        assertEquals(1, musicPojos.size());
        assertEquals(musicNameUrl, musicPojos.get(0).getYoutubeUri());
    }

    /**
     * Purpose: Verify SpotifyMusicLoader class generates a list when give single track Url
     * Input: SpotifyTrackUrl to SpotifyMusicLoader
     *  String(SpotifyTrackUrl)
     * Expected:
     *  size 1, List<MusicPojo>, list has first element like below
     *  MusicPojo {
     *      String title = "musicName";
     *      String youtubeUri = "trackURL";
     *  }
     */
    @Test
    public void SpotifyMusicLoaderTrackLoadTest() {
        List<MusicPojo> spotifyRestReturn = new ArrayList<MusicPojo>();
        MusicPojo track = new MusicPojo("musicName",null);
        spotifyRestReturn.add(track);

        when(restService.getSpotifyMusicName(spotifyTrackUrl)).thenReturn(spotifyRestReturn);
        when(restService.getYoutubeLink(track)).thenReturn(new MusicPojo("musicName", "trackURL"));

        MusicLoader spotifyMusicLoader = MusicLoaderFactory.createMusicLoader(spotifyTrackUrl);
        List<MusicPojo> result = spotifyMusicLoader.getMusicPojos(restService, spotifyTrackUrl, null);

        assertEquals(1, result.size());
        assertEquals("musicName", result.get(0).getTitle());
        assertEquals("trackURL", result.get(0).getYoutubeUri());
    }

    /**
     * Purpose: Verify SpotifyMusicLoader class generates a list when give playlist url
     * Input: spotifyPlaylistUrl to SpotifyMusicLoader
     *  String(spotifyPlaylistUrl)
     * Expected:
     *  size 3, List<MusicPojo>, list has element like below
     *  {
     *      musicPojo("musicName1", "trackURL1"),
     *      musicPojo("musicName2", "trackURL2"),
     *      musicPojo("musicName3", "trackURL3"),
     *  }
     */
    @Test
    public void SpotifyMusicLoaderPlaylistLoadTest() {

        List<MusicPojo> spotifyRestReturn = new ArrayList<MusicPojo>();
        MusicPojo track1 = new MusicPojo("musicName1",null);
        MusicPojo track2 = new MusicPojo("musicName2",null);
        MusicPojo track3 = new MusicPojo("musicName3",null);
        spotifyRestReturn.add(track1);
        spotifyRestReturn.add(track2);
        spotifyRestReturn.add(track3);

        when(restService.getSpotifyMusicName(spotifyPlaylistUrl)).thenReturn(spotifyRestReturn);

        when(restService.getYoutubeLink(track1))
                .thenReturn(new MusicPojo("musicName1", "track1URL"));
        when(restService.getYoutubeLink(track2))
                .thenReturn(new MusicPojo("musicName2", "track2URL"));
        when(restService.getYoutubeLink(track3))
                .thenReturn(new MusicPojo("musicName3", "track3URL"));

        MusicLoader spotifyMusicLoader = MusicLoaderFactory.createMusicLoader(spotifyPlaylistUrl);
        List<MusicPojo> result = spotifyMusicLoader.getMusicPojos(restService, spotifyPlaylistUrl, null);

        assertEquals(3, result.size());

        assertEquals("musicName1", result.get(0).getTitle());
        assertEquals("musicName2", result.get(1).getTitle());
        assertEquals("musicName3", result.get(2).getTitle());

        assertEquals("track1URL", result.get(0).getYoutubeUri());
        assertEquals("track2URL", result.get(1).getYoutubeUri());
        assertEquals("track3URL", result.get(2).getYoutubeUri());
    }

    /**
     * Purpose: Verify MusicLoaderManager class generates a list when give Youtube Video Query
     * Input: youtubeSingleMusicUrl to MusicLoaderManager
     *  String(youtubeSingleMusicUrl)
     * Expected:
     *  size 1, List<MusicPojo>,  first element in this list has same String as youtubeSingleMusicUrl at youtubeUri field
     */
    @Test
    public void MusicLoaderManagerYoutubeTest() {
        when(restService.getYoutubeLink(any(MusicPojo.class))).thenReturn(new MusicPojo("", youtubeSingleMusicUrl));
        List<MusicPojo> result = new MusicLoaderManager().loadMusicUsingQuery(restService, youtubeSingleMusicUrl, null);

        assertEquals(1, result.size());
        assertEquals(youtubeSingleMusicUrl, result.get(0).getYoutubeUri());
    }
}
