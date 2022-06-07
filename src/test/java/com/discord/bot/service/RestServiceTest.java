package com.discord.bot.service;

import com.discord.bot.BotApplication;
import com.discord.bot.entity.pojo.MusicPojo;
import com.discord.bot.loader.MusicLoader;
import com.discord.bot.loader.MusicLoaderManager;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RestServiceTest {
    @Autowired
    RestService restService;

    /**
     * Purpose: Verify that YoutubeRestService receives Uri by sending http requests to match the music name
     * Input: MusicName String ("viva la vida")
     * Expected: Object of MusicPojo
     *   MusicPojo {
     *      String title = "viva la vida";
     *      String youtubeUri = musicNameUrl;
     *   }
     */
    @Test
    public void YoutubeRestServiceTest() {
        restService.trackService.deleteAll();

        String musicName = "viva la vida";
        String musicNameUrl = "https://www.youtube.com/watch?v=HosW0gulISQ";
        MusicPojo pojo = new MusicPojo(musicName, "");
        MusicPojo result = restService.getYoutubeLink(pojo);

        assertEquals( musicName, result.getTitle());
        assertEquals( musicNameUrl, result.getYoutubeUri());
    }

    /**
     * Purpose: Verify that Redis DB interacts well with YoutubeRestService
     * Input: MusicName String ("viva la vida")
     * Expected: Object of MusicPojo
     *   MusicPojo {
     *      String title = "viva la vida";
     *      String youtubeUri = musicNameUrl;
     *   }
     */
    @Test
    public void YoutubeRestSerivceTrackServiceTest() {
        String musicCacheName = "viva la vida";
        String musicCacheNameUrl = "https://www.youtube.com/watch?v=HosW0gulISQ";

        //Reset DB
        restService.trackService.deleteAll();
        //Add MusicPojo in DB that Same as Input
        restService.trackService.cache(musicCacheName, musicCacheNameUrl);
        //Get YoutubeUri from Redis DB
        MusicPojo result = restService.getYoutubeLink(new MusicPojo(musicCacheName, ""));

        assertEquals( musicCacheName, result.getTitle());
        assertEquals( musicCacheNameUrl, result.getYoutubeUri());
    }

    /**
     * Purpose: Verify SpotifyRestService class generates a MusicPojo List when give Spotify Track Url
     * Input: Spotify Track URL to RestService
     *  String(Spotify Track Url)
     *  Track Title : Daft Punk - Get Lucky (feat. Pharrell Williams & Nile Rodgers) - Radio Edit
     * Expected:
     *  size 1, List<MusicPojo>, first element has MusicPojo like below
     *  MusicPojo {
     *      String title = "Daft Punk - Get Lucky (feat. Pharrell Williams & Nile Rodgers) - Radio Edit";
     *      String youtubeUri = null;
     *  }
     */
    @Test
    public void SpotifyRestServiceTrackTest() {
        String spotifyTrackUrl = "https://open.spotify.com/track/2Foc5Q5nqNiosCNqttzHof";
        List<MusicPojo> result = restService.getSpotifyMusicName(spotifyTrackUrl);

        assertEquals( 1, result.size());

        MusicPojo pojo = result.get(0);

        assertEquals("Daft Punk - Get Lucky (feat. Pharrell Williams & Nile Rodgers) - Radio Edit", pojo.getTitle());
        assertNull(pojo.getYoutubeUri());
    }

    /**
     * Purpose: Verify SpotifyRestService class load Multiple MusicPojo when give Spotify Playlist Url
     * Input: Spotify Playlist URL to RestService
     *  String(Spotify Playlist Url that has three track)
     *  Track 1 : Daft Punk - Something About Us
     *  Track 2 : Daft Punk - Harder, Better, Faster, Stronger
     *  Track 3 : Daft Punk - One More Time
     * Expected:
     *  size 3, List<MusicPojo>, three element has MusicPojo like below
     *  MusicPojo {
     *      String title = title of Track X;
     *      String youtubeUri = null;
     *  }
     */
    @Test
    public void SpotifyRestServicePlaylistTest() {
        String spotifyPlaylistUrl = "https://open.spotify.com/playlist/4mVGPbV9WiSoogLFfZC7zn";
        List<MusicPojo> result = restService.getSpotifyMusicName(spotifyPlaylistUrl);

        assertEquals(3, result.size());

        String[] names = {"Daft Punk - Something About Us",
                          "Daft Punk - Harder, Better, Faster, Stronger",
                          "Daft Punk - One More Time"};
        for (int i = 0; i < 3; i++) {
            assertEquals(names[i], result.get(i).getTitle());
            assertNull(result.get(i).getYoutubeUri());
        }
    }
}
