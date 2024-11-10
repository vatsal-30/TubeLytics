package services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import model.Video;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import services.VideoService;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * It is the implementation class of VideoService.
 *
 * @author Yash Ajmeri
 */
public class VideoServiceImpl implements VideoService {
    private final WSClient ws;
    private final String apiKey;
    private static final String YOUTUBE_VIDEO_URL = "https://www.googleapis.com/youtube/v3/videos";


    @Inject
    public VideoServiceImpl(WSClient ws, String apiKey) {
        this.ws = ws;
        this.apiKey = apiKey;
    }

    /**
     * This method will fetch the videos from the YouTube API based on the provided videoId.
     *
     * @author Yash Ajmeri
     */
    @Override
    public CompletionStage<Video> getVideoById(String videoId) {
        return this.ws.url(YOUTUBE_VIDEO_URL)
                .addQueryParameter("part", "snippet")
                .addQueryParameter("id", videoId)
                .addQueryParameter("key", apiKey)
                .get()
                .thenApply(WSResponse::asJson)
                .thenApply(json -> {
                    JsonNode jsonNode = json.get("items");
                    JsonNode item = jsonNode.get(0);

                    String title = item.get("snippet").get("title").asText();
                    String description = item.get("snippet").get("description").asText();
                    String imageUrl = item.get("snippet").get("thumbnails").get("high").get("url").asText();
                    String channelId = item.get("snippet").get("channelId").asText();
                    String channelTitle = item.get("snippet").get("channelTitle").asText();
                    String tags = null;

                    if(item.get("snippet").has("tags")){
                        List<String> valuesAsText = item.get("snippet").findValuesAsText("tags");
                        tags = item.get("snippet").findValue("tags").toString();
                        tags = tags.substring(1, tags.length()-1).replace("\"", "");
                    }



                    return new Video(videoId, title, description, imageUrl, channelId, channelTitle,tags);

                });
    }


}

