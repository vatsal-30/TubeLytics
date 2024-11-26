package actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import model.Video;
import play.libs.Json;
import play.libs.ws.WSClient;

import java.util.concurrent.CompletionStage;

public class VideoServiceActor extends AbstractActor {

    private final WSClient wsClient;
    private final String apiKey;
    private final String youtubeUrl = "https://www.googleapis.com/youtube/v3/videos";

    public VideoServiceActor(WSClient wsClient, String apiKey) {
        this.wsClient = wsClient;
        this.apiKey = apiKey;
    }
    public static Props props(WSClient wsClient, String apiKey) {
        return Props.create(VideoServiceActor.class, () -> new VideoServiceActor( wsClient, apiKey));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, this::fetchVideoDetails)
                .build();
    }

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