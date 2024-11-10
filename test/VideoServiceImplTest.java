

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Video;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import services.impl.VideoServiceImpl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class VideoServiceImplTest {

    @Mock
    private WSClient wsClient;

    @Mock
    private WSRequest wsRequest;

    @Mock
    private WSResponse wsResponse;

    @InjectMocks
    private VideoServiceImpl videoService;

    private final String apiKey = "testApiKey";
    private static final String VIDEO_ID = "sample_video_id";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        videoService = new VideoServiceImpl(wsClient, apiKey);
    }

    @Test
    public void testGetVideoById() throws Exception {
        // Sample JSON response from YouTube API
        String jsonResponse = """
            {
                "items": [
                    {
                        "snippet": {
                            "title": "Sample Video Title",
                            "description": "Sample description for the video",
                            "thumbnails": {
                                "high": {
                                    "url": "https://example.com/sample.jpg"
                                }
                            },
                            "channelId": "sample_channel_id",
                            "channelTitle": "Sample Channel Title",
                            "tags": ["tag1", "tag2", "tag3"]
                        }
                    }
                ]
            }
            """;

        // Setup mock behaviors
        when(wsClient.url(anyString())).thenReturn(wsRequest);
        when(wsRequest.addQueryParameter(anyString(), anyString())).thenReturn(wsRequest);
        when(wsRequest.get()).thenReturn(CompletableFuture.completedFuture(wsResponse));

        // Mocking the response JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonResponse);
        when(wsResponse.asJson()).thenReturn(jsonNode);

        // Call the method
        CompletionStage<Video> videoStage = videoService.getVideoById(VIDEO_ID);
        Video video = videoStage.toCompletableFuture().get();

        // Assertions
        assertNotNull(video);
        assertEquals(VIDEO_ID, video.getVideoId());
        assertEquals("Sample Video Title", video.getTitle());
        assertEquals("Sample description for the video", video.getDescription());
        assertEquals("https://example.com/sample.jpg", video.getImageUrl());
        assertEquals("sample_channel_id", video.getChannelId());
        assertEquals("Sample Channel Title", video.getChannelTitle());
        assertEquals("tag1,tag2,tag3", video.getTags());
    }
}
