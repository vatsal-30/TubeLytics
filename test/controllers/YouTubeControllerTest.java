package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import model.Response;
import model.SearchForm;
import model.Video;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;
import services.VideoService;
import services.YouTubeService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static play.test.Helpers.*;

public class YouTubeControllerTest extends WithApplication {
    private Response response;

    @Mock
    private YouTubeService youTubeService;

    @Mock
    private VideoService videoService;

    @Mock
    private FormFactory formFactory;

    @Mock
    private Form<SearchForm> searchForm;

    private YouTubeController youTubeController;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(formFactory.form(SearchForm.class)).thenReturn(searchForm);
        youTubeController = new YouTubeController(youTubeService, formFactory, videoService);

        response = new Response();
        response.setQuery("Sample query about education and learning");
        response.setAverageFleschKincaidGradeLevel(7.36);
        response.setAverageFleschReadingScore(70.24);
        response.setVideos(new ArrayList<>());

        Video video = new Video("vid-001", "Understanding the Basics of Reading Levels", "An introduction to understanding reading levels and their impact on learning", "https://example.com/image1.jpg", "channel-001", "Education Today");
        video.setFleschKincaidGradeLevel(6.2);
        video.setFleschReadingScore(65.0);
        response.getVideos().add(video);

        video = new Video("vid-002", "Tips for Improving Reading Comprehension", "Tips for Improving Reading Comprehension", "https://example.com/image2.jpg", "channel-002", "Learning Insights");
        video.setFleschKincaidGradeLevel(8.1);
        video.setFleschReadingScore(70.3);
        response.getVideos().add(video);

        video = new Video("vid-003", "Advanced Strategies for Teaching Reading Skills", "Discover advanced strategies for teaching reading skills effectively.", "https://example.com/image3.jpg", "channel-003", "Teacher's Hub");
        video.setFleschKincaidGradeLevel(9.4);
        video.setFleschReadingScore(75.8);
        response.getVideos().add(video);

        video = new Video("vid-004", "The Importance of Reading for Young Learners", "Explores why reading is essential for early childhood education.", "https://example.com/image4.jpg", "channel-004", "Kids Academy");
        video.setFleschKincaidGradeLevel(5.8);
        video.setFleschReadingScore(68.2);
        response.getVideos().add(video);

        video = new Video("vid-005", "How to Foster a Love for Reading", "Ways to encourage a lifelong passion for reading in children.", "https://example.com/image5.jpg", "channel-005", "Book Lovers");
        video.setFleschKincaidGradeLevel(7.3);
        video.setFleschReadingScore(71.9);
        response.getVideos().add(video);
    }

    @Test
    public void searchTest() throws ExecutionException, InterruptedException {
        Http.RequestBuilder requestBuilder = new Http.RequestBuilder()
                .method(POST)
                .uri("/search")
                .bodyJson(Json.newObject().put("query", "Sample query about education and learning"));

        Http.RequestImpl request = requestBuilder.build();
        when(searchForm.bindFromRequest(request)).thenReturn(searchForm);
        SearchForm form = new SearchForm();
        form.setQuery("Sample query about education and learning");
        when(searchForm.bindFromRequest(request).get()).thenReturn(form);
        when(youTubeService.searchVideos(any(String.class))).thenReturn(CompletableFuture.supplyAsync(() -> response));
        CompletionStage<Result> resultCompletionStage = youTubeController.search(request);
        Result result = resultCompletionStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        assertEquals("application/json", result.contentType().orElse(""));
        JsonNode jsonResponse = Json.parse(Helpers.contentAsString(result));

        String query = jsonResponse.get("query").asText();
        assertEquals("Sample query about education and learning", query);

        double averageFleschKincaidGradeLevel = jsonResponse.get("averageFleschKincaidGradeLevel").asDouble();
        assertEquals(Double.valueOf(7.36), Double.valueOf(averageFleschKincaidGradeLevel));

        double averageFleschReadingScore = jsonResponse.get("averageFleschReadingScore").asDouble();
        assertEquals(Double.valueOf(70.24), Double.valueOf(averageFleschReadingScore));

        JsonNode jsonNode = jsonResponse.get("videos");

        if (jsonNode.isArray()) {
            List<JsonNode> nodeList = new ArrayList<>();
            for (JsonNode node : jsonNode) {
                nodeList.add(node);
            }
            assertEquals(5, nodeList.size());
        }
    }

    @Test
    public void indexTest() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/");

        Result result = route(app, request);
        assertEquals(OK, result.status());
    }
}
