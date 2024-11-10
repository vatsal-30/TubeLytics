import com.typesafe.config.Config;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.libs.ws.WSClient;
import services.VideoService;
import services.YouTubeService;

import static org.mockito.Mockito.verify;

import services.impl.VideoServiceImpl;
import services.impl.YouTubeServiceImpl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ModuleTest {
    @Mock
    private WSClient wsClient;

    @Mock
    private Config config;

    @InjectMocks
    private Module module;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(config.getString("youtube.api.key")).thenReturn("API_KEY");
    }

    @Test
    public void youTubeServiceProviderTest() {
        YouTubeService youtubeService = module.provideYouTubeService(wsClient);

        assertNotNull(youtubeService);
        assertTrue(youtubeService instanceof YouTubeServiceImpl);

        verify(config).getString("youtube.api.key");
    }

    @Test
    public void videoServiceProviderTest() {
        VideoService videoService = module.provideVideoService(wsClient);

        assertNotNull(videoService);
        assertTrue(videoService instanceof VideoServiceImpl);

        verify(config).getString("youtube.api.key");
    }
}
