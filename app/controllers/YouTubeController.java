package controllers;

import model.SearchForm;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.YouTubeService;

import javax.inject.Inject;

import java.util.concurrent.CompletionStage;

public class YouTubeController extends Controller {
    private final YouTubeService youTubeService;
    private final Form<SearchForm> searchForm;

    @Inject
    public YouTubeController(YouTubeService youTubeService, FormFactory formFactory) {
        this.youTubeService = youTubeService;
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
}