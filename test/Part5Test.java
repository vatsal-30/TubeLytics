
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.test.WithApplication;
import services.YouTubeService;
import services.impl.YouTubeServiceImpl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Utsav Patel
 */
public class Part5Test extends WithApplication {
    private String jsonResponse;

    @Mock
    private WSClient wsClient;

    @Mock
    private WSRequest wsRequest;

    @Mock
    private WSResponse wsResponse;

    private YouTubeService youTubeService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * This method sets up a dummy json response to test YouTubeService.
     *
     * @author Utsav Patel
     */
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        youTubeService = new YouTubeServiceImpl(wsClient, "api_key");
        jsonResponse = """
                {
                    "items": [
                        {
                            "id": {
                                "videoId": "vid-001"
                            },
                            "snippet": {
                                "title": "Understanding the Basics of Reading Levels",
                                "description": "An introduction to understanding reading levels and their impact on learning",
                                "thumbnails": {
                                    "high": {
                                        "url": "https://example.com/image1.jpg"
                                    }
                                },
                                "channelId": "channel-001",
                                "channelTitle": "Education Today"
                            }
                        },
                        {
                            "id": {
                                "videoId": "vid-002"
                            },
                            "snippet": {
                                "title": "Tips for Improving Reading Comprehension",
                                "description": "Tips for Improving Reading Comprehension.",
                                "thumbnails": {
                                    "high": {
                                        "url": "https://example.com/image2.jpg"
                                    }
                                },
                                "channelId": "channel-002",
                                "channelTitle": "Learning Insights"
                            }
                        }
                    ]
                }
                """;
    }

    /**
     * This method tests searchVideos method of YouTubeService.
     *
     * @author Utsav Patel
     */
    @Test
    public void testSearchVideoByKeyword() throws ExecutionException, InterruptedException, JsonProcessingException {
        when(wsClient.url(anyString())).thenReturn(wsRequest);
        when(wsRequest.addQueryParameter(anyString(), anyString())).thenReturn(wsRequest);
        when(wsRequest.get()).thenReturn(CompletableFuture.completedFuture(wsResponse));
        when(wsResponse.asJson()).thenReturn(objectMapper.readTree(jsonResponse));
        CompletionStage<Response> responseCompletionStage = youTubeService.searchVideos("Sample query about education and learning");
        Assert.assertNotNull(responseCompletionStage);
        Response response = responseCompletionStage.toCompletableFuture().get();

        Assert.assertNotNull(response);
        Assert.assertEquals("Sample query about education and learning", response.getQuery());
        Assert.assertEquals(2, response.getVideos().size());

        assertEquals("vid-001", response.getVideos().get(0).getVideoId());
        assertEquals("vid-002", response.getVideos().get(1).getVideoId());
    }
}
