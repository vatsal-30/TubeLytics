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

/**
 *
 * @author Karan Tanakhia
 */
public class YouTubeServiceImplPart2Test {

    @Mock
    private WSClient mockWsClient;

    @Mock
    private WSRequest mockWsRequest;

    @Mock
    private WSResponse mockWsResponse;

    @InjectMocks
    private YouTubeServiceImpl youTubeService;

    /**
     * This constant `MOCK_JSON_RESPONSE` defines a mock JSON string that simulates a YouTube API response, including video ID, title, description, thumbnail URL, channel ID, and channel title, for testing purposes.
     *
     * @author Karan Tanakhia
     */
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

    /**
     * This setup method initializes mocks for a YouTube service test, configuring a mock `WSClient` to simulate HTTP requests and responses, including JSON handling, for testing purposes.
     * @author Karan Tanakhia
     */
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

    /**
     * This test verifies that `wordStatesVideos` method correctly returns a list of word statistics for a given keyword, asserting the size and specific content of the output.
     *
     * @author Karan Tanakhia
     */
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
