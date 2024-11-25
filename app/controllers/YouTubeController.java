package controllers;

import actor.DescriptionReadabilityActor;
import actor.SupervisorActor;
import actor.TimeActor;
import actor.WebSocketActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.Materializer;
import com.typesafe.config.Config;
import model.SearchForm;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.libs.streams.ActorFlow;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.WebSocket;
import services.YouTubeService;
import services.VideoService;

import javax.inject.Inject;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * @author Utsav Patel
 */
public class YouTubeController extends Controller {
    private final YouTubeService youTubeService;
    private final VideoService videoService;
    private final ActorSystem actorSystem;
    private final Materializer materializer;
    private final WSClient wsClient;
    private final String API_KEY;
    private final Form<SearchForm> searchForm;
    private final ActorRef supervisorActor;

    @Inject
    public YouTubeController(YouTubeService youTubeService, FormFactory formFactory, VideoService videoService, ActorSystem actorSystem, Materializer materializer, WSClient wsClient, Config config) {
        this.youTubeService = youTubeService;
        this.videoService = videoService;
        this.searchForm = formFactory.form(SearchForm.class);
        this.actorSystem = actorSystem;
        this.materializer = materializer;
        this.wsClient = wsClient;
        this.API_KEY = config.getString("youtube.api.key");
        actorSystem.actorOf(TimeActor.getProps(), "timeActor");
        actorSystem.actorOf(DescriptionReadabilityActor.props(), "descriptionReadability");
        this.supervisorActor = actorSystem.actorOf(SupervisorActor.props(wsClient, API_KEY), "supervisor");
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

    public WebSocket ws() {
        return WebSocket.Text.accept(request -> ActorFlow.actorRef(
                actorRef -> {
                    String ID = UUID.randomUUID().toString();
                    Props webSocketProps = WebSocketActor.props(this.supervisorActor, ID, actorRef);
                    ActorRef webSocketActor = actorSystem.actorOf(webSocketProps, "websocket-" + ID);
                    supervisorActor.tell(new WebSocketActor.NewConnection(webSocketActor), actorRef);
                    return webSocketProps;
                },
                actorSystem,
                materializer
        ));
    }

    /**
     * This will return the JSON response of the Video class based on the videoID.
     *
     * @author Yash Ajmeri
     */
    public CompletionStage<Result> showVideoDetails(String videoId) {
        return videoService.getVideoById(videoId)
                .thenApply(video -> ok(views.html.videoDetailsPage.render(video)));
    }

    /**
     * This will return the JSON response of the Response class based on the tags.
     *
     * @author Yash Ajmeri
     */
    public CompletionStage<Result> searchTags(String searchTag) {
        return youTubeService.searchVideos(searchTag)
                .thenApply(response -> ok(views.html.taggedVideo.render(response)));
    }

    /**
     * This will return the JSON response of the Video class based on the ChannelId.
     *
     * @author Amish Navadia
     */
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
