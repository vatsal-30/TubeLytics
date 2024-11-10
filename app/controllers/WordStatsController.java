package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import services.YouTubeService;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

/**
 * @author Karan Tanakhia
 */
public class WordStatsController extends Controller {

    private final YouTubeService youTubeService;

    @Inject
    public WordStatsController(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    /**
     * This will return the JSON response of the keyword and description of searched videos from that keywords and their frequency.
     *
     * @author Karan Tanakhia
     */
    public CompletionStage<Result> getWordStats(String keyword) {


        return youTubeService.wordStatesVideos(keyword)
                .thenApply(wordStats-> ok(views.html.wordStats.render(keyword, wordStats)));
    }

}

