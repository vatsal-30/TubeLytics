package services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import model.ChannelProfile;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.StreamSupport;

public class YouTubeServiceImpl implements YouTubeService {

    private final WSClient ws;
    private final String apiKey;
    private static final String YOUTUBE_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search";
    private static final String YOUTUBE_VIDEO_URL = "https://www.googleapis.com/youtube/v3/videos";

    @Inject
    public YouTubeServiceImpl(WSClient ws, String apiKey) {
        this.ws = ws;
        this.apiKey = apiKey;
    }

    @Override
    public CompletionStage<Response> searchVideos(String keyword) {
        WSRequest request = ws.url(YOUTUBE_SEARCH_URL)
                .addQueryParameter("part", "snippet")
                .addQueryParameter("q", keyword)
                .addQueryParameter("type", "video")
                .addQueryParameter("maxResults", "10")
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
                    response.setQuery(keyword);
                    response.setVideos(videoStream.toList());
                    return response;
                });

        responseStage = responseStage
                .thenCompose(response -> {
                    AtomicReference<Double> fkg = new AtomicReference<>(0.0);
                    AtomicReference<Double> frs = new AtomicReference<>(0.0);
                    List<CompletableFuture<Void>> completionStageList = response.getVideos().stream()
                            .map(video -> fetchDescription(video.getVideoId())
                                    .thenAccept(fullDescription -> {
                                        double[] readabilityScores = calculateReadabilityScores(fullDescription);
                                        fkg.updateAndGet(v -> (v + readabilityScores[0]));
                                        video.setFleschKincaidGradeLevel(readabilityScores[0]);
                                        frs.updateAndGet(v -> (v + readabilityScores[1]));
                                        video.setFleschReadingScore(readabilityScores[1]);
                                        video.setDescription(fullDescription);
                                    }).toCompletableFuture()).toList();

                    CompletableFuture<Void> allUpdates = CompletableFuture.allOf(
                            completionStageList.toArray(new CompletableFuture[0])
                    );

                    return allUpdates.thenApply(ignored -> {
                        int size = response.getVideos().size();
                        response.setAverageFleschKincaidGradeLevel(fkg.get() / size);
                        response.setAverageFleschReadingScore(frs.get() / size);
                        return response;
                    });
                });

//        return Source.completionStage(listCompletionStage)
//                .flatMapConcat(Source::from)
//                .mapAsync(2, this::fetchDescription);
        // TODO : PART 4 AND 5
//        return Source.completionStage(listCompletionStage)
//                .flatMapConcat(Source::from);
        return responseStage;
    }


//    This is my Amish Part
    public CompletionStage<ChannelProfile> getChannelProfile(String channelId) {
        String apiUrl = "https://www.googleapis.com/youtube/v3/channels?part=snippet,statistics&id=" + channelId + "&key=AIzaSyC0fC9nUXNPtREfQqS3kqnr4TJIee3Ay_Q";

        return ws.url(apiUrl)
                .get()
                .thenApply(response -> {
                    // Process the response and create a ChannelProfile object
                    String name = response.asJson().get("items").get(0).get("snippet").get("title").asText();
                    String imageUrl = response.asJson().get("items").get(0).get("snippet").get("thumbnails")
                            .get("default").get("url").asText();
                    String description = response.asJson().get("items").get(0).get("snippet").get("description").asText();
                    String subscriberCount = response.asJson().get("items").get(0).get("statistics")
                            .get("subscriberCount").asText();
                    String videoCount = response.asJson().get("items").get(0).get("statistics")
                            .get("videoCount").asText();

                    // Return a new ChannelProfile object with fetched data
                    return new ChannelProfile(name, imageUrl, description, subscriberCount, videoCount);
                });
    }


    private CompletionStage<String> fetchDescription(String id) {
        return this.ws.url(YOUTUBE_VIDEO_URL)
                .addQueryParameter("part", "snippet")
                .addQueryParameter("id", id)
                .addQueryParameter("key", apiKey)
                .get()
                .thenApply(WSResponse::asJson)
                .thenApply(json -> {
                    JsonNode item = json.get("items");
                    String fullDescription = item.get(0).findPath("snippet").findPath("description").asText();
                    return fullDescription;
                });
    }

    private static double[] calculateReadabilityScores(String description) {
        String[] words = splitIntoWords(description);
        int totalWords = words.length;
        int totalSentences = countSentences(description);

        int totalSyllables = 0;

        for (String word : words) {
            totalSyllables += countSyllables(word);
        }

        if (totalWords == 0 || totalSentences == 0) {
            return new double[]{0, 0};
        }

        double wordsPerSentence = (double) totalWords / totalSentences;
        double syllablesPerWord = (double) totalSyllables / totalWords;
        double fkg = 0.39 * wordsPerSentence + 11.8 * syllablesPerWord - 15.59;
        double frs = 206.835 - 1.015 * wordsPerSentence - 84.6 * syllablesPerWord;
        return new double[]{fkg, frs};
    }

    private static int countSyllables(String word) {
        word = word.toLowerCase().trim();
        if (word.length() == 1) {
            return 1;
        }

        if (word.endsWith("e")) {
            word = word.substring(0, word.length() - 1);
        }

        String[] vowelGroups = word.split("[^aeiouy]+");
        int syllableCount = 0;
        for (String group : vowelGroups) {
            if (!group.isEmpty()) {
                syllableCount++;
            }
        }

        if (word.length() > 2 && word.endsWith("le") && isConsonant(word.charAt(word.length() - 3))) {
            syllableCount++;
        }

        if (word.endsWith("ed") && syllableCount > 1) {
            if (word.length() > 2 && isConsonant(word.charAt(word.length() - 3))) {
                syllableCount--;
            }
        }

        if (word.endsWith("es") && syllableCount > 1) {
            if (word.length() > 2 && isConsonant(word.charAt(word.length() - 3))) {
                syllableCount--;
            }
        }

        return Math.max(syllableCount, 1);
    }

    private static boolean isConsonant(char c) {
        return "bcdfghjklmnpqrstvwxyz".indexOf(c) >= 0;
    }

    private static String[] splitIntoWords(String text) {
        return text.split("\\s+");
    }

    private static int countSentences(String text) {
        String[] sentences = text.split("[.!?;:]");
        return sentences.length;
    }
}
