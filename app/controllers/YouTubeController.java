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
import model.Video;

import javax.inject.Inject;

import java.util.concurrent.CompletionStage;

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

    public Result index(Http.Request request) {
        return ok(views.html.index.render(searchForm, request));
    }

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


}
