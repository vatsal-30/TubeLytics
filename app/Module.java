import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import play.Environment;
import play.libs.ws.WSClient;
import services.VideoService;
import services.YouTubeService;
import services.impl.VideoServiceImpl;
import services.impl.YouTubeServiceImpl;

/**
 * @author Utsav Patel
 */
public class Module extends AbstractModule {
    private final Environment environment;
    private final Config config;

    public Module(Environment environment, Config config) {
        this.environment = environment;
        this.config = config;
    }

    @Override
    protected void configure() {
//        bind(YouTubeService.class).to(YouTubeServiceImpl.class);
//        bind(YouTubeService.class).toInstance(new YouTubeServiceImpl(play.libs.ws.WS.newClient(-1)));
    }

    /**
     * It is the provider for YouTubeService.
     *
     * @author Utsav Patel
     */
    @Provides
    public YouTubeService provideYouTubeService(WSClient wsClient) {
        String apiKey = config.getString("youtube.api.key");
        return new YouTubeServiceImpl(wsClient, apiKey);
    }

    /**
     * It is the provider for VideoService.
     * @author Yash Ajmeri
     */
    @Provides
    public VideoService provideVideoService(WSClient wsClient) {
        String apiKey = config.getString("youtube.api.key");
        return new VideoServiceImpl(wsClient, apiKey);
    }
}
