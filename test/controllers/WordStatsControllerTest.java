package controllers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.mvc.Result;
import services.YouTubeService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;

/**
 * @author Karan Tanakhia
 */
public class WordStatsControllerTest {

    @Mock
    private YouTubeService mockYouTubeService;

    @InjectMocks
    private YouTubeController youTubeController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * This method tests wordstats for the given keyword.
     *
     * @author Karan Tanakhia
     */
//    @Test
//    public void testGetWordStats_validKeyword() {
//        // Arrange
//        String keyword = "example";
//        List<String> mockWordStats = List.of("word1: 5", "word2: 3", "word3: 1");
//
//        // Mock the service response
//        when(mockYouTubeService.wordStatesVideos(anyString()))
//                .thenReturn(CompletableFuture.completedFuture(mockWordStats));
//
//        // Act
//        CompletionStage<Result> resultStage = youTubeController.getWordStats(keyword);
//        Result result = resultStage.toCompletableFuture().join();
//
//
//        // Assert
//        assertEquals(OK, result.status());
//        String content = contentAsString(result);
//        assert content.contains("<h1>Word Stats for \"example\"</h1>");
//        assert content.contains("<td>word1</td>");
//        assert content.contains("<td>5</td>");
//        assert content.contains("<td>word2</td>");
//        assert content.contains("<td>3</td>");
//        assert content.contains("<td>word3</td>");
//        assert content.contains("<td>1</td>");
//        for (String stat : mockWordStats) {
//            assert content.contains(stat.split(":")[0]);  // Check word (e.g., "word1")
//            assert content.contains(stat.split(":")[1].trim());
//        }
//    }
}
