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


/**
 * This actor class handles YouTube video searches based on tags.
 * It communicates with the YouTube Data API to fetch videos matching the provided tag
 * and sends the result back to the original sender in the form of a {@link Response}.
 *
 * @author Yash Ajmeri
 */

public class TaggedServiceActor  extends  AbstractActor {

    private final WSClient wsClient;
    private final String apiKey;
    private final String youtubeUrl = "https://www.googleapis.com/youtube/v3/search";

    /**
     * Constructs a TaggedServiceActor with the specified WSClient and API key.
     *
     * @param wsClient the Play WSClient used for making HTTP requests
     * @param apiKey   the YouTube API key used for authentication
     * @author Yash Ajmeri
     */

    public TaggedServiceActor(WSClient wsClient, String apiKey) {
        this.wsClient = wsClient;
        this.apiKey = apiKey;
    }
    /**
     * Factory method to create Props for this actor.
     *
     * @param wsClient the Play WSClient used for making HTTP requests
     * @param apiKey   the YouTube API key used for authentication
     * @return a Props instance for creating the actor
     * @author Yash Ajmeri
     */

    public static Props props(WSClient wsClient, String apiKey) {
        return Props.create(TaggedServiceActor.class, () -> new TaggedServiceActor( wsClient, apiKey));
    }
    /**
     * Defines the behavior of this actor.
     *
     * @return the Receive object defining the message handling behavior
     * @author Yash Ajmeri
     */

    @Override
    public Receive createReceive() {
        return  receiveBuilder()
                .match(String.class, this::onSearchByTag)
                .build();
    }
    /**
     * Handles a search request by tag.
     * Sends a query to the YouTube API, processes the response, and sends back a {@link Response}.
     *
     * @param tag the search query (tag) for finding videos
     * @author Yash Ajmeri
     */
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
