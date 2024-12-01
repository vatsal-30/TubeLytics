package controllers;

import akka.actor.ActorRef;
import model.SearchForm;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.data.FormFactory;
import play.mvc.Result;
import services.VideoService;
import services.YouTubeService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static play.test.Helpers.contentAsString;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import play.libs.ws.WSClient;
import static play.test.Helpers.*;

import play.api.Configuration;
import com.typesafe.config.Config;
import play.data.Form;


public class YouTubeControllerTest1 {

    @Mock
    private YouTubeService mockYouTubeService;

    @InjectMocks
    private YouTubeController youTubeController;

    @Mock
    private VideoService videoService;

    @Mock
    private FormFactory formFactory;

    @Mock
    private ActorSystem actorSystem;

    @Mock
    private WSClient wsClient;

    @Mock
    private Materializer materializer;

    @Mock
    private Configuration mockConfig;
    @Mock
    private Config mockConfigOb;

    @Mock
    private ActorRef wordStatsActor;

    private Form<SearchForm> searchForm;


    /**
     * Initializes the mock objects and sets up the YouTubeController instance.
     * This method is executed before each test to ensure the tests are isolated.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);  // Initialize mocks

//        wordStatsActor = mock(ActorRef.class);
        List<String> mockWordStats = List.of("word1: 5", "word2: 3", "word3: 1");

        CompletableFuture<List<String>> mockFuture = CompletableFuture.completedFuture(mockWordStats);
//        Future<Object> future = (Future<Object>) mockFuture;
//        when(Patterns.ask(eq(wordStatsActor), eq("example"), eq(2000L)))
//                .thenReturn(mockFuture);

        when(mockYouTubeService.wordStatesVideos(eq("example")))
                .thenReturn(mockFuture);


        youTubeController = new YouTubeController(mockYouTubeService, formFactory, videoService, actorSystem, materializer, wsClient, mockConfigOb);

    }

//    @Test
//    public void testGetWordStats_validKeyword() {
//        // Arrange: setup test data
//        String keyword = "example";
//        List<String> mockWordStats = List.of("word1: 5", "word2: 3", "word3: 1");
//
//        when(Patterns.ask(eq(wordStatsActor), eq(keyword), eq(2000)))
//                .thenReturn((Future<Object>) CompletableFuture.completedFuture(mockWordStats));
//
//
//        CompletableFuture<List<String>> mockFuture = CompletableFuture.completedFuture(mockWordStats);
//        when(mockYouTubeService.wordStatesVideos(eq(keyword))).thenReturn(mockFuture);
//        // Mock the service reasponse
////        when(mockYouTubeService.wordStatesVideos(anyString()))
////                .thenReturn(CompletableFuture.completedFuture(mockWordStats));
//
//        // Act: make a request to the controller
////        CompletionStage<Result> resultStage = youTubeController.getWordStats(keyword);
////        Result result = resultStage.toCompletableFuture().join();
//
//        Result result = youTubeController.getWordStats(keyword).toCompletableFuture().join();
//        // Assert: verify the response status and content
//        assertEquals(OK, result.status());
//
//        String content = contentAsString(result);
//        assert content.contains("<h1>Word Stats for \"example\"</h1>");
//        assert content.contains("<td>word1</td>");
//        assert content.contains("<td>5</td>");
//        assert content.contains("<td>word2</td>");
//        assert content.contains("<td>3</td>");
//        assert content.contains("<td>word3</td>");
//        assert content.contains("<td>1</td>");
//
//        for (String stat : mockWordStats) {
//            assert content.contains(stat.split(":")[0]);  // Check word (e.g., "word1")
//            assert content.contains(stat.split(":")[1].trim());  // Check count (e.g., "5")
//        }
//    }


    /**
     * Tests the scenario where the service throws an error when fetching word statistics.
     * Ensures that the controller handles the error and returns a 500 Internal Server Error status.
     */
    @Test
    public void testGetWordStats_serviceError() {
        when(mockYouTubeService.wordStatesVideos(anyString()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Service error")));

        CompletionStage<Result> resultStage = youTubeController.getWordStats("errorKeyword");
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(500, result.status());
        String content = contentAsString(result);
        assert content.contains("Failed to fetch word statistics");
    }

    /**
     //     * Tests the scenario where an empty keyword is provided to the controller.
     //     * Ensures that the controller returns a 400 Bad Request status and appropriate error message.
     //     */
    @Test
    public void testGetWordStatsWithEmptyKeyword() {
        String keyword = "";
        List<String> mockWordStats = List.of();

        when(mockYouTubeService.wordStatesVideos(anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockWordStats));

        CompletionStage<Result> resultStage = youTubeController.getWordStats(keyword);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(BAD_REQUEST, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Keyword cannot be empty"));
    }

}
