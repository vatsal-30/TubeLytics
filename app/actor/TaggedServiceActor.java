package actor;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import model.Response;
import model.Video;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import java.util.concurrent.CompletionStage;
import java.util.stream.StreamSupport;

public class TaggedServiceActor  extends  AbstractActor {

    private final WSClient wsClient;
    private final String apiKey;
    private final String youtubeUrl = "https://www.googleapis.com/youtube/v3/search";

    public TaggedServiceActor(WSClient wsClient, String apiKey) {
        this.wsClient = wsClient;
        this.apiKey = apiKey;
    }
    public static Props props(WSClient wsClient, String apiKey) {
        return Props.create(TaggedServiceActor.class, () -> new TaggedServiceActor( wsClient, apiKey));
    }

    @Override
    public Receive createReceive() {
        return  receiveBuilder()
                .match(String.class, this::onSearchByTag)
                .build();
    }
    public void onSearchByTag(String tag) {
        ActorRef originalSender = sender();
        WSRequest request = wsClient.url(youtubeUrl)
                .addQueryParameter("part", "snippet")
                .addQueryParameter("q", tag)
                .addQueryParameter("type", "video")
                .addQueryParameter("maxResults", "50")
                .addQueryParameter("order", "date")
                .addQueryParameter("key", apiKey);

        CompletionStage<Response> responseStage = request.get()
                .thenApply(WSResponse::asJson)
                .thenApply(json -> {
                    Iterable<JsonNode> items = json.get("items");
                    return StreamSupport.stream(items.spliterator(), false)
                            .map(item -> {
                                String videoId = item.get("id").get("videoId").asText();
                                String title = item.get("snippet").get("title").asText();
                                String description = item.get("snippet").get("description").asText();
                                String imageUrl = item.get("snippet").get("thumbnails").get("high").get("url").asText();
                                String channelId = item.get("snippet").get("channelId").asText();
                                String channelTitle = item.get("snippet").get("channelTitle").asText();
                                return new Video(videoId, title, description, imageUrl, channelId, channelTitle);
                            });
                })
                .thenApply(videoStream -> {
                    Response response = new Response();
                    response.setQuery(tag);
                    response.setVideos(videoStream.toList());
                    return response;
                });
        responseStage.thenAccept(response -> originalSender.tell(response, self()))
                .exceptionally(ex -> {
                    // Handle errors and send back an error message or null
                    originalSender.tell(ex.getMessage(), self());
                    return null;
                });

    }
}
