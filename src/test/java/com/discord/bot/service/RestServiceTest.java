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


}
