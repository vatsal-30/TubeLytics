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

public class UserActor extends AbstractActor {
    private final ActorRef actorRef;
    private final WSClient ws;
    private final String API_KEY;
    private final List<String> searchHistory = new ArrayList<>();
    private static final String YOUTUBE_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search";
    private static final String YOUTUBE_VIDEO_URL = "https://www.googleapis.com/youtube/v3/videos";

    public UserActor(ActorRef actorRef, WSClient wsClient, String apiKey) {
        this.actorRef = actorRef;
        this.ws = wsClient;
        this.API_KEY = apiKey;
    }

    public static Props props(ActorRef actorRef, WSClient wsClient, String apiKey) {
        return Props.create(UserActor.class, () -> new UserActor(actorRef, wsClient, apiKey));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, message -> {
                    if (!searchHistory.contains(message)) {
                        searchHistory.add(message);
                    }
                    // TODO: Then Sentiment
                    this.searchVideos(message)
                            .toCompletableFuture()
                            .thenAccept(response -> {
                                this.calculateScore(response)
                                        .thenAccept(responseWithScore -> {
                                            if (responseWithScore != null) {
                                                actorRef.tell(serializeResponse(responseWithScore), getSelf());
                                            } else {
                                                actorRef.tell(serializeResponse(response), getSelf());
                                            }
                                        });
                            });
                })
                .match(SupervisorActor.NotifyClient.class, notifyClient -> {
                    searchHistory
                            .forEach(keyword -> {
                                searchVideos(keyword)
                                        .toCompletableFuture()
                                        .thenAccept(response -> {
                                            actorRef.tell(serializeResponse(response), getSelf());
                                        });
                            });
                })
                .build();
    }

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
                }).exceptionally(e -> {
                    System.out.println("ERROR : Unable to Fetch Videos.");
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

    public CompletionStage<Response> calculateScore(Response response) {
        String actorPath = "akka://application/user/descriptionReadability";
        ActorSelection actorSelection = context().actorSelection(actorPath);

        return actorSelection.resolveOne(Duration.ofSeconds(2)).toCompletableFuture()
                .thenCompose(actorRef -> FutureConverters.asJava(ask(actorRef, response, 2000))
                        .thenApply(calculatedDescriptionScoreResponse -> {
                            if (calculatedDescriptionScoreResponse instanceof Response) {
                                return (Response) calculatedDescriptionScoreResponse;
                            }
                            return null;
                        }));
    }

    public String serializeResponse(Response response) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize response", e);
        }
    }

}
