package actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import model.Video;
import play.libs.Json;
import play.libs.ws.WSClient;

import java.util.concurrent.CompletionStage;

/**
 * Actor responsible for fetching detailed information about a YouTube video.
 * Communicates with the YouTube Data API to retrieve metadata for a given video ID
 * and returns the result to the sender as a {@link Video} object.
 *
 * @author Yash Ajmeri
 */

public class VideoServiceActor extends AbstractActor {

    private final WSClient wsClient;
    private final String apiKey;
    private final String youtubeUrl = "https://www.googleapis.com/youtube/v3/videos";

    /**
     * Constructs a VideoServiceActor with the specified WSClient and API key.
     *
     * @param wsClient the Play WSClient used for making HTTP requests
     * @param apiKey   the YouTube API key used for authentication
     * @author Yash Ajmeri
     */

    public VideoServiceActor(WSClient wsClient, String apiKey) {
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
        return Props.create(VideoServiceActor.class, () -> new VideoServiceActor( wsClient, apiKey));
    }
    /**
     * Defines the behavior of this actor.
     *
     * @return the Receive object defining the message handling behavior
     * @author Yash Ajmeri
     */

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, this::fetchVideoDetails)
                .build();
    }

    /**
     * Fetches video details from the YouTube Data API for a given video ID.
     * Sends the retrieved {@link Video} object back to the original sender.
     *
     * @param videoId the unique identifier for the YouTube video to fetch details for
     * @author Yash Ajmeri
     */

    public void fetchVideoDetails(String videoId) {
        ActorRef originalSender = sender();
        CompletionStage<Video> videoDetails = wsClient.url(youtubeUrl)
                .addQueryParameter("part", "snippet")
                .addQueryParameter("id", videoId)
                .addQueryParameter("key", apiKey)
                .get()
                .thenApply(response -> response.asJson().get("items").get(0))
                .thenApply(item -> {

                    JsonNode snippet = item.get("snippet");


//                    return Json.newObject()
//                            .put("videoId", videoId)
//                            .put("title", snippet.get("title").asText())
//                            .put("description", snippet.get("description").asText())
//                            .put("imageUrl", snippet.get("thumbnails").get("high").get("url").asText())
//                            .put("channelTitle", snippet.get("channelTitle").asText())
//                            .put("tags",tags!=null ? tags : "");


                    String title = snippet.get("title").asText();
                    String description = snippet.get("description").asText();
                    String imageUrl = snippet.get("thumbnails").get("high").get("url").asText();
                    String channelId = snippet.get("channelId").asText();
                    String channelTitle = snippet.get("channelTitle").asText();
                    String tags =null;
                    if(snippet.has("tags")) {
                        tags = snippet.findValue("tags").toString();
                        tags = tags.substring(1, tags.length()-1).replace("\"", "");
                    }
//                    System.out.println(videoId +" " +title+" " +description+" " +imageUrl+" " +channelId+" " +channelTitle+" " +tags);
                    return new Video(videoId,title,description,imageUrl,channelId,channelTitle,tags);
                });

//        videoDetails.thenAccept(details -> getSender().tell(details, getSelf()));
        videoDetails.thenAccept(video -> originalSender.tell(video, self()))
                .exceptionally(ex -> {
                    // Handle errors and send back an error message or null
                    originalSender.tell(ex.getMessage(), self());
                    return null;
                });
}
}