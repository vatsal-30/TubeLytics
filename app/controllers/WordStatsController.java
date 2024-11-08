package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import services.YouTubeService;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class WordStatsController extends Controller {

    private final YouTubeService youTubeService;

    @Inject
    public WordStatsController(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    public CompletionStage<Result> getWordStats(String keyword) {


        return youTubeService.wordStatesVideos(keyword)
                .thenApply(wordStats-> ok(views.html.wordStats.render(keyword, wordStats)));
    }

}

