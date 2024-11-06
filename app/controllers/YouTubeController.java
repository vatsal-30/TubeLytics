package controllers;

import akka.stream.javadsl.Sink;
import play.mvc.Controller;
import play.mvc.Result;
import services.YouTubeService;

import javax.inject.Inject;

import akka.actor.ActorSystem;
import akka.stream.Materializer;

public class YouTubeController extends Controller {
    private final YouTubeService youTubeService;

    @Inject
    public YouTubeController(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    public Result search() {
        ActorSystem system = ActorSystem.create();
        Materializer materializer = Materializer.createMaterializer(system);
        this.youTubeService.searchVideos("Java").runWith(Sink.foreach(element -> System.out.println("Element: " + element)), materializer);
        return ok(views.html.index.render());
    }
}
