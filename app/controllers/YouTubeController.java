package controllers;

import model.SearchForm;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.YouTubeService;
import services.VideoService;

import javax.inject.Inject;

import java.util.concurrent.CompletionStage;

/**
 * @author Utsav Patel
 */
public class YouTubeController extends Controller {
    private final YouTubeService youTubeService;
    private final VideoService videoService;
    private final Form<SearchForm> searchForm;

    @Inject
    public YouTubeController(YouTubeService youTubeService, FormFactory formFactory, VideoService videoService) {
        this.youTubeService = youTubeService;
        this.videoService = videoService;
        this.searchForm = formFactory.form(SearchForm.class);
    }

    /**
     * This method will render the index page, which contains a search field.
     *
     * @author Utsav Patel
     */
    public Result index(Http.Request request) {
        return ok(views.html.index.render(searchForm, request));
    }

    /**
     * This will return the JSON response of the Response class based on the search query.
     *
     * @author Utsav Patel
     */
    public CompletionStage<Result> search(Http.Request request) {
        SearchForm form = searchForm.bindFromRequest(request).get();
        String keyword = form.getQuery();
        return youTubeService
                .searchVideos(keyword)
                .thenApply(response -> ok(Json.toJson(response)));
    }

    public CompletionStage<Result> showVideoDetails(String videoId) {
        return videoService.getVideoById(videoId)
                .thenApply(video -> ok(views.html.videoDetailsPage.render(video)));
    }

    public CompletionStage<Result> searchTags(String searchTag) {
        return youTubeService.searchVideos(searchTag)
                .thenApply(response -> ok(views.html.taggedVideo.render(response)));
    }

    // Method to get channel profile
    public CompletionStage<Result> channelProfile(String channelId) {

        return youTubeService.getChannelProfile(channelId) // Fetch channel profile
                .thenCompose(channelProfile -> {
                    return youTubeService.getChannelVideos(channelId, 10) // Fetch the 10 latest videos
                            .thenApply(videos -> {
                                channelProfile.setVideos(videos);  // Add videos to the channel profile
                                return ok(views.html.channelProfile.render(channelProfile)); // Pass to the view
                            });
                });
    }
}
