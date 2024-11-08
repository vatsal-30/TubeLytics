package services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import model.Response;
import model.Video;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import services.YouTubeService;

import javax.inject.Inject;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import services.SentimentAnalyzer;



public class YouTubeServiceImpl implements YouTubeService {

    private final WSClient ws;
    private final String apiKey;
    private static final String YOUTUBE_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search";
    private static final String YOUTUBE_VIDEO_URL = "https://www.googleapis.com/youtube/v3/videos";
    private final SentimentAnalyzer sentimentAnalyzer = new SentimentAnalyzer(); // Create an instance of SentimentAnalyzer

    @Inject
    public YouTubeServiceImpl(WSClient ws, String apiKey) {
        this.ws = ws;
        this.apiKey = apiKey;
    }

//     @Override
//     public CompletionStage<Response> searchVideos(String keyword) {
//         WSRequest request = ws.url(YOUTUBE_SEARCH_URL)
//                 .addQueryParameter("part", "snippet")
//                 .addQueryParameter("q", keyword)
//                 .addQueryParameter("type", "video")
//                 .addQueryParameter("maxResults", "10")
//                 .addQueryParameter("key", apiKey);

//         CompletionStage<Response> responseStage = request.get()
//                 .thenApply(WSResponse::asJson)
//                 .thenApply(json -> {
//                     Iterable<JsonNode> items = json.get("items");
//                     return StreamSupport.stream(items.spliterator(), false)
//                             .map(item -> {
//                                 String videoId = item.get("id").get("videoId").asText();
//                                 String title = item.get("snippet").get("title").asText();
//                                 String description = item.get("snippet").get("description").asText();
//                                 String imageUrl = item.get("snippet").get("thumbnails").get("high").get("url").asText();
//                                 String channelId = item.get("snippet").get("channelId").asText();
//                                 String channelTitle = item.get("snippet").get("channelTitle").asText();
//                                 return new Video(videoId, title, description, imageUrl, channelId, channelTitle);
//                             })
//                             .collect(Collectors.toList());
//                 }).thenApply(videos -> {
//                     Response response = new Response();
//                     response.setQuery(keyword);
//                     response.setVideos(videos);
//                     return response;
//                 });

// //        return Source.completionStage(listCompletionStage)
// //                .flatMapConcat(Source::from)
// //                .mapAsync(2, this::fetchDescription);
//         // TODO : PART 4 AND 5
// //        return Source.completionStage(listCompletionStage)
// //                .flatMapConcat(Source::from);
//         return responseStage;
//     }

        @Override
        public CompletionStage<Response> searchVideos(String keyword) {
            WSRequest request = ws.url(YOUTUBE_SEARCH_URL)
                    .addQueryParameter("part", "snippet")
                    .addQueryParameter("q", keyword)
                    .addQueryParameter("type", "video")
                    .addQueryParameter("maxResults", "10")
                    .addQueryParameter("key", apiKey);

            // Fetch basic video details (truncated descriptions)
            return request.get()
                    .thenApply(WSResponse::asJson)
                    .thenComposeAsync(json -> {
                        Iterable<JsonNode> items = json.get("items");

                        // Create a list of Video objects
                        List<Video> videos = StreamSupport.stream(items.spliterator(), false)
                                .map(item -> {
                                    String videoId = item.get("id").get("videoId").asText();
                                    String title = item.get("snippet").get("title").asText();
                                    String description = item.get("snippet").get("description").asText();
                                    String imageUrl = item.get("snippet").get("thumbnails").get("high").get("url").asText();
                                    String channelId = item.get("snippet").get("channelId").asText();
                                    String channelTitle = item.get("snippet").get("channelTitle").asText();
                                    return new Video(videoId, title, description, imageUrl, channelId, channelTitle);
                                })
                                .collect(Collectors.toList());

                        // Use fetchFullDescription to get full descriptions for each video
                        List<CompletionStage<Video>> descriptionStages = videos.stream()
                                .map(video -> fetchFullDescription(video.getVideoId())
                                        .thenApply(fullDescription -> {
                                            video.setDescription(fullDescription);
                                            return video;
                                        }))
                                .collect(Collectors.toList());

                        // Wait for all description fetches to complete
                        return CompletableFuture.allOf(descriptionStages.toArray(new CompletableFuture[0]))
                                .thenApply(v -> {
                                    // Perform sentiment analysis on the full descriptions
                                    List<String> descriptions = videos.stream()
                                            .map(Video::getDescription)
                                            .collect(Collectors.toList());

                                    String sentimentResult = sentimentAnalyzer.analyzeSentiment(descriptions);

                                    // Set the sentiment in the response
                                    Response response = new Response();
                                    response.setQuery(keyword);
                                    response.setVideos(videos);
                                    response.setSentiment(sentimentResult);
                                    return response;
                                });
                    });
        }


    private CompletionStage<Video> fetchDescription(Video video) {
        return this.ws.url(YOUTUBE_VIDEO_URL)
                .addQueryParameter("part", "snippet")
                .addQueryParameter("id", video.getVideoId())
                .addQueryParameter("key", apiKey)
                .get()
                .thenApply(WSResponse::asJson)
                .thenApply(json -> {
                    JsonNode item = json.get("items");
                    String fullDescription = item.get(0).findPath("snippet").findPath("description").asText();
                    video.setDescription(fullDescription);
                    return video;
                });
    }

    private CompletionStage<String> fetchFullDescription(String id) {
        return this.ws.url(YOUTUBE_VIDEO_URL)
                .addQueryParameter("part", "snippet")
                .addQueryParameter("id", id)
                .addQueryParameter("key", apiKey)
                .get()
                .thenApply(WSResponse::asJson)
                .thenApply(json -> {
                    JsonNode item = json.get("items");
                    String fullDescription = item.get(0).findPath("snippet").findPath("description").asText();
//                    video.setDescription(fullDescription);
//                    return video;
                    return fullDescription;
                });
    }
    
}
