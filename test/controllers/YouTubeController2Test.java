package controllers;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import model.ChannelProfile;
import model.Response;
import model.SearchForm;
import model.Video;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import services.YouTubeService;
import services.VideoService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;

/**
 * @author Yash Ajmeri
 */
public class YouTubeController2Test {

    @Mock
    private YouTubeService youTubeService;

    @Mock
    private VideoService videoService;

    @Mock
    private FormFactory formFactory;

    @Mock
    private Form<SearchForm> searchForm;

    @Mock
    private Config config;

    @Mock
    private ActorSystem actorSystem;

    @InjectMocks
    private YouTubeController youTubeController;

    private final String VIDEO_ID = "sample_video_id";
    private final String CHANNEL_ID = "sample_channel_id";
    private final String SEARCH_KEYWORD = "sample_keyword";

    /**
     * This `setUp` method initializes mock annotations and configures the mock `formFactory` to return a `SearchForm` instance when the `form` method is called with `SearchForm.class`, for testing purposes.
     *
     * @author Yash Ajmeri
     */
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(formFactory.form(SearchForm.class)).thenReturn(searchForm);
        when(config.getString(anyString())).thenReturn("api_key");
        when(actorSystem.actorOf(any())).thenReturn(null);
        when(actorSystem.actorOf(any(), anyString())).thenReturn(null);
    }


    /**
     * This test checks that the `showVideoDetails` method in `YouTubeController` correctly retrieves and displays video details, verifying that the response status is OK and the content is not null.
     *
     * @author Yash Ajmeri
     */


//    @Test
//    public void testShowVideoDetails() throws Exception {
//        Video mockVideo = new Video(VIDEO_ID, "Sample Video", "Sample description", "https://example.com/sample.jpg", CHANNEL_ID, "Sample Channel");
//        when(videoService.getVideoById(VIDEO_ID)).thenReturn(CompletableFuture.completedFuture(mockVideo));
//
//        CompletionStage<Result> resultStage = youTubeController.showVideoDetails(VIDEO_ID);
//        Result result = resultStage.toCompletableFuture().get();
//
//        assertEquals(OK, result.status());
//        assertNotNull(contentAsString(result));
//    }

    /**
     * This test verifies that the `searchTags` method in `YouTubeController` correctly retrieves and displays search results for a given tag, asserting an OK status and non-null content in the response.
     *
     * @author Yash Ajmeri
     */
    @Test
    public void testSearchTags() throws Exception {
        Response mockResponse = new Response();
        mockResponse.setQuery("sampleTag");
        mockResponse.setVideos(List.of(new Video(VIDEO_ID, "Sample Video", "Sample description", "https://example.com/sample.jpg", CHANNEL_ID, "Sample Channel")));

        when(youTubeService.searchVideos("sampleTag")).thenReturn(CompletableFuture.completedFuture(mockResponse));

        CompletionStage<Result> resultStage = youTubeController.searchTags("sampleTag");
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        assertNotNull(contentAsString(result));
    }

    /**
     * This test confirms that the `channelProfile` method in `YouTubeController` correctly retrieves and displays a channel profile and its videos, checking for an OK status and non-null content in the response.
     *
     * @author Amish Navadia
     */
    @Test
    public void testChannelProfile() throws Exception {
        ChannelProfile mockProfile = new ChannelProfile("Sample Channel", "https://example.com/channel.jpg", "Channel description", "1000", "50", List.of());
        List<Video> videos = List.of(new Video(VIDEO_ID, "Sample Video", "Sample description", "https://example.com/sample.jpg", CHANNEL_ID, "Sample Channel"));

        when(youTubeService.getChannelProfile(CHANNEL_ID)).thenReturn(CompletableFuture.completedFuture(mockProfile));
        when(youTubeService.getChannelVideos(CHANNEL_ID, 10)).thenReturn(CompletableFuture.completedFuture(videos));

        CompletionStage<Result> resultStage = youTubeController.channelProfile(CHANNEL_ID);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        assertNotNull(contentAsString(result));
    }
}
