
package services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.ChannelProfile;
import model.Response;
import model.Video;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class YouTubeServiceImplPart1Test {

    @Mock
    private WSClient wsClient;

    @Mock
    private WSRequest wsRequest;

    @Mock
    private WSResponse wsResponse;

    @InjectMocks
    private YouTubeServiceImpl youTubeService;

    private final String apiKey = "testApiKey";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        youTubeService = new YouTubeServiceImpl(wsClient, apiKey);
    }


    @Test
    public void testGetChannelProfile() throws Exception {
        String channelId = "channel123";
        String jsonResponse = """
            {
                "items": [
                    {
                        "snippet": {
                            "title": "Education Channel",
                            "description": "A channel dedicated to educational content.",
                            "thumbnails": {
                                "default": {
                                    "url": "https://example.com/channel_thumbnail.jpg"
                                }
                            }
                        },
                        "statistics": {
                            "subscriberCount": "1000",
                            "videoCount": "50"
                        }
                    }
                ]
            }
            """;

        // Setup mock behaviors
        when(wsClient.url(anyString())).thenReturn(wsRequest);
        when(wsRequest.addQueryParameter(anyString(), anyString())).thenReturn(wsRequest);
        when(wsRequest.get()).thenReturn(CompletableFuture.completedFuture(wsResponse));
        when(wsResponse.asJson()).thenReturn(new ObjectMapper().readTree(jsonResponse));

        // Call the method
        CompletionStage<ChannelProfile> profileStage = youTubeService.getChannelProfile(channelId);
        ChannelProfile profile = profileStage.toCompletableFuture().get();

        // Assertions
        assertNotNull(profile);
        assertEquals("Education Channel", profile.getName());
        assertEquals("https://example.com/channel_thumbnail.jpg", profile.getImageUrl());
        assertEquals("A channel dedicated to educational content.", profile.getDescription());
        assertEquals("1000", profile.getSubscriberCount());
        assertEquals("50", profile.getVideoCount());
        assertNotNull(profile.getVideos());
        assertTrue(profile.getVideos().isEmpty());
    }

    @Test
    public void testGetChannelVideos() throws Exception {
        String channelId = "channel123";
        int maxResults = 2;
        String jsonResponse = """
            {
                "items": [
                    {
                        "id": {
                            "videoId": "vid123"
                        },
                        "snippet": {
                            "title": "Educational Video 1",
                            "description": "First educational video.",
                            "thumbnails": {
                                "high": {
                                    "url": "https://example.com/thumbnail1.jpg"
                                }
                            },
                            "channelTitle": "Education Channel"
                        }
                    },
                    {
                        "id": {
                            "videoId": "vid456"
                        },
                        "snippet": {
                            "title": "Educational Video 2",
                            "description": "Second educational video.",
                            "thumbnails": {
                                "high": {
                                    "url": "https://example.com/thumbnail2.jpg"
                                }
                            },
                            "channelTitle": "Education Channel"
                        }
                    }
                ]
            }
            """;

        // Setup mock behaviors
        when(wsClient.url(anyString())).thenReturn(wsRequest);
        when(wsRequest.addQueryParameter(anyString(), anyString())).thenReturn(wsRequest);
        when(wsRequest.get()).thenReturn(CompletableFuture.completedFuture(wsResponse));
        when(wsResponse.asJson()).thenReturn(new ObjectMapper().readTree(jsonResponse));

        // Call the method
        CompletionStage<List<Video>> videosStage = youTubeService.getChannelVideos(channelId, maxResults);
        List<Video> videos = videosStage.toCompletableFuture().get();

        // Assertions
        assertNotNull(videos);
        assertEquals(2, videos.size());

        Video video1 = videos.get(0);
        assertEquals("vid123", video1.getVideoId());
        assertEquals("Educational Video 1", video1.getTitle());
        assertEquals("First educational video.", video1.getDescription());
        assertEquals("https://example.com/thumbnail1.jpg", video1.getImageUrl());
        assertEquals("Education Channel", video1.getChannelTitle());

        Video video2 = videos.get(1);
        assertEquals("vid456", video2.getVideoId());
        assertEquals("Educational Video 2", video2.getTitle());
        assertEquals("Second educational video.", video2.getDescription());
        assertEquals("https://example.com/thumbnail2.jpg", video2.getImageUrl());
        assertEquals("Education Channel", video2.getChannelTitle());
    }



}
