package actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Response;
import model.Video;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import scala.jdk.javaapi.FutureConverters;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.StreamSupport;

import static akka.pattern.Patterns.ask;

/**
 * UserActor handles requests for YouTube video searches, sentiment analysis, and score calculations.
 * <br/>
 * Functionality:
 * - Manages user search history
 * - Performs YouTube video searches
 * - Analyzes video sentiments
 * - Calculates readability scores
 * - Serializes responses to JSON
 *
 * @author Utsav Patel
 */
public class UserActor extends AbstractActor {
    private static final String YOUTUBE_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search";
    private static final String YOUTUBE_VIDEO_URL = "https://www.googleapis.com/youtube/v3/videos";
    private final ActorRef actorRef;
    private final WSClient ws;
    private final String API_KEY;
    private final List<String> searchHistory = new ArrayList<>();

    /**
     * Constructor for UserActor.
     *
     * @param actorRef Reference to the WebSocket actor for communication with the client
     * @param wsClient Play WSClient instance for making HTTP requests
     * @param apiKey   YouTube API key for authenticating requests
     * @author Utsav Patel
     */
    public UserActor(ActorRef actorRef, WSClient wsClient, String apiKey) {
        this.actorRef = actorRef;
        this.ws = wsClient;
        this.API_KEY = apiKey;
    }

    /**
     * Factory method for creating Props for UserActor.
     *
     * @param actorRef Reference to the WebSocket actor
     * @param wsClient Play WSClient instance
     * @param apiKey   YouTube API key
     * @return Props instance for UserActor
     * @author Utsav Patel
     */
    public static Props props(ActorRef actorRef, WSClient wsClient, String apiKey) {
        return Props.create(UserActor.class, () -> new UserActor(actorRef, wsClient, apiKey));
    }

    /**
     * Defines the behavior of the UserActor.
     *
     * @return Receive instance defining message handling logic
     * @author Utsav Patel
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, message -> {
                    if (!searchHistory.contains(message)) {
                        if (searchHistory.size() >= 10) {
                            searchHistory.remove(0);
                        }
                        searchHistory.add(message);
                    } else {
                        searchHistory.remove(message);
                        searchHistory.add(message);
                    }
                    this.searchVideos(message)
                            .toCompletableFuture()
                            .thenAccept(response -> {
                                response.setFromKeyword(true);

                                this.analyzeSentiments(response)
                                        .thenAccept(responseWithSentiment -> {
                                            this.calculateScore(responseWithSentiment == null ? response : responseWithSentiment)
                                                    .thenAccept(responseWithScore -> {
                                                        if (responseWithScore != null) {
                                                            actorRef.tell(serializeResponse(responseWithScore), getSelf());
                                                        } else {
                                                            actorRef.tell(serializeResponse(responseWithSentiment == null ? response : responseWithSentiment), getSelf());
                                                        }
                                                    });
                                        });
                            });
                })
                .match(SupervisorActor.NotifyClient.class, notifyClient -> {
                    searchHistory
                            .forEach(keyword -> {
                                searchVideos(keyword)
                                        .toCompletableFuture()
                                        .thenAccept(response -> {
                                            response.setFromKeyword(false);
                                            this.analyzeSentiments(response)
                                                    .thenAccept(responseWithSentiment -> {
                                                        this.calculateScore(responseWithSentiment == null ? response : responseWithSentiment)
                                                                .thenAccept(responseWithScore -> {
                                                                    if (responseWithScore != null) {
                                                                        actorRef.tell(serializeResponse(responseWithScore), getSelf());
                                                                    } else {
                                                                        actorRef.tell(serializeResponse(responseWithSentiment == null ? response : responseWithSentiment), getSelf());
                                                                    }
                                                                });
                                                    });
                                        });
                            });
                })
                .build();
    }

    /**
     * Searches YouTube videos based on the given keyword.
     *
     * @param keyword Search keyword
     * @return CompletionStage containing the search response
     * @author Utsav Patel
     */
    public CompletionStage<Response> searchVideos(String keyword) {
        WSRequest request = ws.url(YOUTUBE_SEARCH_URL)
                .addQueryParameter("part", "snippet")
                .addQueryParameter("q", keyword)
                .addQueryParameter("type", "video")
                .addQueryParameter("maxResults", "50")
                .addQueryParameter("order", "date")
                .addQueryParameter("key", API_KEY);

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
                    response.setQuery(keyword);
                    response.setVideos(videoStream.toList());
                    return response;
                })
                .exceptionally(e -> {
                    System.err.println("ERROR: Unable to fetch videos for keyword: " + keyword + " - " + e.getMessage());
                    getSender().tell("ERROR: Unable to fetch videos", getSelf());
                    return null;
                });


        responseStage = responseStage
                .thenCompose(response -> {
                    List<CompletableFuture<Void>> completionStageList = response.getVideos().stream()
                            .map(video -> fetchDescription(video.getVideoId())
                                    .thenAccept(video::setDescription).toCompletableFuture()).toList();

                    CompletableFuture<Void> allUpdates = CompletableFuture.allOf(
                            completionStageList.toArray(new CompletableFuture[0])
                    );
                    return allUpdates.thenApply(ignored -> response);
                });
        return responseStage;
    }

    /**
     * Fetches the full description of a YouTube video by its ID.
     *
     * @param id Video ID
     * @return CompletionStage containing the full description
     * @author Utsav Patel
     */
    public CompletionStage<String> fetchDescription(String id) {
        return this.ws.url(YOUTUBE_VIDEO_URL)
                .addQueryParameter("part", "snippet")
                .addQueryParameter("id", id)
                .addQueryParameter("key", API_KEY)
                .get()
                .thenApply(WSResponse::asJson)
                .thenApply(json -> {
                    JsonNode item = json.get("items");
                    String fullDescription = item.get(0).findPath("snippet").findPath("description").asText();
                    return fullDescription;
                });
    }

    /**
     * Calculates readability score for the given response.
     *
     * @param response Sentiment-analyzed response
     * @return CompletionStage containing the response with calculated score
     * @author Utsav Patel
     */
    public CompletionStage<Response> calculateScore(Response response) {
        String actorPath = "akka://application/user/descriptionReadability";
        ActorSelection actorSelection = context().actorSelection(actorPath);

        return actorSelection.resolveOne(Duration.ofSeconds(2)).toCompletableFuture()
                .exceptionally(e -> {
                    System.err.println("ERROR: Failed to resolve descriptionReadability actor: " + e.getMessage());
                    return null;
                })
                .thenCompose(actorRef -> actorRef != null
                        ? FutureConverters.asJava(ask(actorRef, response, 2000)).thenApply(calculatedResponse -> {
                    if (calculatedResponse instanceof Response) {
                        return (Response) calculatedResponse;
                    }
                    return null;
                })
                        : CompletableFuture.completedFuture(null));
    }

    /**
     * Analyzes sentiments for the given response.
     *
     * @param response Search response
     * @return CompletionStage containing the sentiment-analyzed response
     * @author Utsav Patel
     */
    public CompletionStage<Response> analyzeSentiments(Response response) {
        String actorPath = "akka://application/user/sentimentalAnalyzer";
        ActorSelection actorSelection = context().actorSelection(actorPath);

        return actorSelection.resolveOne(Duration.ofSeconds(2)).toCompletableFuture()
                .exceptionally(e -> {
                    System.err.println("ERROR: Failed to resolve sentimentalAnalyzer actor: " + e.getMessage());
                    return null;
                })
                .thenCompose(actorRef -> actorRef != null
                        ? FutureConverters.asJava(ask(actorRef, response, 2000))
                        .thenApply(analyzedResponse -> {
                            if (analyzedResponse instanceof Response) {
                                return (Response) analyzedResponse;
                            }
                            return null;
                        }) : CompletableFuture.completedFuture(null));
    }

    /**
     * Serializes a Response object to a JSON string.
     *
     * @param response Response object
     * @return Serialized JSON string
     * @author Utsav Patel
     */
    public static String serializeResponse(Response response) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize response", e);
        }
    }
}
