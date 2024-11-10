package services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class YouTubeServiceImplPart2Test {

    @Mock
    private WSClient mockWsClient;

    @Mock
    private WSRequest mockWsRequest;

    @Mock
    private WSResponse mockWsResponse;

    @InjectMocks
    private YouTubeServiceImpl youTubeService;

    private static final String MOCK_JSON_RESPONSE = """
        {
            "items": [
                {
                    "id": { "videoId": "testVideoId" },
                    "snippet": {
                        "title": "Test Title",
                        "description": "Test Description",
                        "thumbnails": { "high": { "url": "http://example.com/image.jpg" }},
                        "channelId": "testChannelId",
                        "channelTitle": "Test Channel Title"
                    }
                }
            ]
        }
    """;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        youTubeService = new YouTubeServiceImpl(mockWsClient, "dummyApiKey");

        // Mock the WSClient to return a WSRequest
        when(mockWsClient.url(anyString())).thenReturn(mockWsRequest);

        // Mock addQueryParameter to return mockWsRequest for method chaining
        when(mockWsRequest.addQueryParameter(anyString(), anyString())).thenReturn(mockWsRequest);

        // Mock get() call to return a WSResponse
        when(mockWsRequest.get()).thenReturn(CompletableFuture.completedFuture(mockWsResponse));

        // Handle JsonProcessingException and mock JSON response
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(MOCK_JSON_RESPONSE);
            when(mockWsResponse.asJson()).thenReturn(jsonNode);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWordStatesVideos_returnsWordStats() {
        String keyword = "example";

        // Act
        CompletionStage<List<String>> resultStage = youTubeService.wordStatesVideos(keyword);
        List<String> wordStats = resultStage.toCompletableFuture().join();

        // Assert
        assertEquals(2, wordStats.size()); // Adjust based on expected output
        assertEquals("test: 1", wordStats.get(0)); // Check word count output as expected
    }
}
