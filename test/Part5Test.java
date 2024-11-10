
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

public class Part5Test extends WithApplication {

    private final static String TEXT = "In a quaint little village nestled between misty mountains and sprawling green fields, there was a peculiar shop that everyone called \"The Whispering Lantern.\" The shop appeared ordinary from the outside, with a simple wooden sign swaying gently in the breeze, but inside, it was anything but. Shelves brimmed with enchanted trinkets, ancient scrolls, and curious artifacts from distant lands. Visitors claimed that each item had a story to tell, and if you listened closely, you could hear faint whispers echoing through the lanterns hanging from the ceiling.";
    //    private Response response;
    private String jsonResponse;

    @Mock
    private WSClient wsClient;

    @Mock
    private WSRequest wsRequest;

    @Mock
    private WSResponse wsResponse;

    private YouTubeService youTubeService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

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
        assertTrue(10 < response.getAverageFleschKincaidGradeLevel());
        assertTrue(12 > response.getAverageFleschKincaidGradeLevel());
        assertTrue(30 < response.getAverageFleschReadingScore());
        assertTrue(40 > response.getAverageFleschReadingScore());

        assertEquals("vid-001", response.getVideos().get(0).getVideoId());
        assertEquals("vid-002", response.getVideos().get(1).getVideoId());
    }

    @Test
    public void countSentenceTest() {
        int countSentences = YouTubeServiceImpl.countSentences(TEXT);
        Assert.assertEquals(4, countSentences);
    }

    @Test
    public void splitIntoWordsTest() {
        String[] words = YouTubeServiceImpl.splitIntoWords(TEXT);
        Assert.assertNotNull(words);
        Assert.assertEquals(88, words.length);
        Assert.assertEquals("In", words[0]);
        Assert.assertEquals("a", words[1]);
    }

    @Test
    public void consonantTest() {
        Assert.assertFalse(YouTubeServiceImpl.isConsonant('a'));
        Assert.assertTrue(YouTubeServiceImpl.isConsonant('b'));
    }

    @Test
    public void countSyllablesTest() {
        assertEquals(1, YouTubeServiceImpl.countSyllables("a"));
        assertEquals(1, YouTubeServiceImpl.countSyllables("cake"));
        assertEquals(3, YouTubeServiceImpl.countSyllables("elephant"));
        assertEquals(1, YouTubeServiceImpl.countSyllables("table"));
        assertEquals(3, YouTubeServiceImpl.countSyllables("tablee"));
        assertEquals(2, YouTubeServiceImpl.countSyllables("taalee"));
        assertEquals(1, YouTubeServiceImpl.countSyllables("whale"));
        assertEquals(2, YouTubeServiceImpl.countSyllables("waleed"));
        assertEquals(1, YouTubeServiceImpl.countSyllables("played"));
        assertEquals(1, YouTubeServiceImpl.countSyllables("watches"));
        assertEquals(2, YouTubeServiceImpl.countSyllables("agrees"));
        assertEquals(1, YouTubeServiceImpl.countSyllables("rhythm"));
        assertEquals(2, YouTubeServiceImpl.countSyllables("HeLLo "));
        assertEquals(3, YouTubeServiceImpl.countSyllables("beautiful"));
        assertEquals(1, YouTubeServiceImpl.countSyllables("red"));
        assertEquals(1, YouTubeServiceImpl.countSyllables("yes"));
        assertEquals(1, YouTubeServiceImpl.countSyllables("aeiou"));
    }

    @Test
    public void calculateReadabilityScoresTest() {
        double[] calculateReadabilityScores = YouTubeServiceImpl.calculateReadabilityScores(TEXT);
        // Between 10 and 12
        Assert.assertTrue(10 < calculateReadabilityScores[0]);
        Assert.assertTrue(12 > calculateReadabilityScores[0]);

        // Between 50 and 60
        Assert.assertTrue(50 < calculateReadabilityScores[1]);
        Assert.assertTrue(60 > calculateReadabilityScores[1]);

        calculateReadabilityScores = YouTubeServiceImpl.calculateReadabilityScores("");
        Assert.assertEquals(Double.valueOf(0.0), Double.valueOf(calculateReadabilityScores[0]));
        Assert.assertEquals(Double.valueOf(0.0), Double.valueOf(calculateReadabilityScores[1]));
    }
}
