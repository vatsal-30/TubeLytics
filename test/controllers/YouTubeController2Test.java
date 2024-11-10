package controllers;

import com.fasterxml.jackson.databind.JsonNode;
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

public class YouTubeController2Test {

    @Mock
    private YouTubeService youTubeService;

    @Mock
    private VideoService videoService;

    @Mock
    private FormFactory formFactory;

    @Mock
    private Form<SearchForm> searchForm;

    @InjectMocks
    private YouTubeController youTubeController;

    private final String VIDEO_ID = "sample_video_id";
    private final String CHANNEL_ID = "sample_channel_id";
    private final String SEARCH_KEYWORD = "sample_keyword";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(formFactory.form(SearchForm.class)).thenReturn(searchForm);
    }






    @Test
    public void testShowVideoDetails() throws Exception {
        Video mockVideo = new Video(VIDEO_ID, "Sample Video", "Sample description", "https://example.com/sample.jpg", CHANNEL_ID, "Sample Channel");
        when(videoService.getVideoById(VIDEO_ID)).thenReturn(CompletableFuture.completedFuture(mockVideo));

        CompletionStage<Result> resultStage = youTubeController.showVideoDetails(VIDEO_ID);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        assertNotNull(contentAsString(result));
    }

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
