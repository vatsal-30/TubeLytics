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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import services.SentimentAnalyzer;

/**
 * It is the implementation class of YouTubeService.
 *
 * @author Utsav Patel
 */
public class YouTubeServiceImpl implements YouTubeService {

    private final WSClient ws;
    private final String apiKey;
    private static final String YOUTUBE_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search";
    private static final String YOUTUBE_VIDEO_URL = "https://www.googleapis.com/youtube/v3/videos";
    private static final String YOUTUBE_CHANNEL_URL = "https://www.googleapis.com/youtube/v3/channels";

    @Inject
    public YouTubeServiceImpl(WSClient ws, String apiKey) {
        this.ws = ws;
        this.apiKey = apiKey;
    }

    /**
     * This method will fetch the videos from the YouTube API based on the provided keyword.
     *
     * @param keyword to filter out videos based on it.
     * @return CompletionStage of Response
     * @author Utsav Patel
     */
    @Override
    public CompletionStage<Response> searchVideos(String keyword) {

        SentimentAnalyzer sentimentAnalyzer = new SentimentAnalyzer();

        WSRequest request = ws.url(YOUTUBE_SEARCH_URL)
                .addQueryParameter("part", "snippet")
                .addQueryParameter("q", keyword)
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
                    response.setQuery(keyword);
                    response.setVideos(videoStream.toList());
                    return response;
                });

        responseStage = responseStage
                .thenCompose(response -> {
                    AtomicReference<Double> fkg = new AtomicReference<>(0.0);
                    AtomicReference<Double> frs = new AtomicReference<>(0.0);

                    List<String> descriptions = new ArrayList<>();

                    List<CompletableFuture<Void>> completionStageList = response.getVideos().stream()
                            .map(video -> fetchDescription(video.getVideoId())
                                    .thenAccept(fullDescription -> {
                                        double[] readabilityScores = calculateReadabilityScores(fullDescription);

                                        descriptions.add(fullDescription);

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

                        String sentimentResult = sentimentAnalyzer.analyzeSentiment(descriptions);
                        response.setSentiment(sentimentResult);

                        return response;
                    });
                });
        return responseStage;
    }

    /**
     * This method will fetch the Channel profile from the YouTube API based on the provided channelId.
     *
     * @author Amish Navadia
     */
    public CompletionStage<ChannelProfile> getChannelProfile(String channelId) {
        return this.ws.url(YOUTUBE_CHANNEL_URL)
                .addQueryParameter("part", "snippet,statistics")
                .addQueryParameter("id", channelId)
                .addQueryParameter("key", apiKey)
                .get()
                .thenApply(response -> {
                    String name = response.asJson().get("items").get(0).get("snippet").get("title").asText();
                    String imageUrl = response.asJson().get("items").get(0).get("snippet").get("thumbnails")
                            .get("default").get("url").asText();
                    String description = response.asJson().get("items").get(0).get("snippet").get("description").asText();
                    String subscriberCount = response.asJson().get("items").get(0).get("statistics")
                            .get("subscriberCount").asText();
                    String videoCount = response.asJson().get("items").get(0).get("statistics")
                            .get("videoCount").asText();

                    // Return a new ChannelProfile object with fetched data
                    return new ChannelProfile(name, imageUrl, description, subscriberCount, videoCount, new ArrayList<>());
                });
    }

    /**
     * This method will fetch the Channel's latest 10 video from the YouTube API based on the provided channelId.
     *
     * @author Amish Navadia
     */
    @Override
    public CompletionStage<List<Video>> getChannelVideos(String channelId, int i) {
        return this.ws.url(YOUTUBE_SEARCH_URL)
                .addQueryParameter("part", "snippet")
                .addQueryParameter("channelId", channelId)
                .addQueryParameter("order", "date")
                .addQueryParameter("type", "video")
                .addQueryParameter("maxResults", Integer.toString(i))
                .addQueryParameter("key", apiKey)
                .get()
                .thenApply(WSResponse::asJson)
                .thenApply(json -> {
                    Iterable<JsonNode> items = json.get("items");

                    return StreamSupport.stream(items.spliterator(), false)
                            .map(item -> {
                                String videoId = item.get("id").get("videoId").asText();
                                String title = item.get("snippet").get("title").asText();
                                String description = item.get("snippet").get("description").asText();
                                String imageUrl = item.get("snippet").get("thumbnails").get("high").get("url").asText();
                                String channelTitle = item.get("snippet").get("channelTitle").asText();

                                return new Video(videoId, title, description, imageUrl, channelId, channelTitle);
                            }).toList();
                });
    }

    /**
     * This method will fetch the full description of the YouTube video using the YouTube API. 
     *
     * @param id id of the video
     * @return CompletionStage of String (full description)
     * @author Utsav Patel
     */
    public CompletionStage<String> fetchDescription(String id) {
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
    /**
     * This method will calculate the readability score.
     * Flesch-Kincaid Grade Level
     * Flesch Reading Score
     *
     * @param description description of video
     * @return double[] - It contains the readability score
     * @author Utsav Patel
     */
    public static double[] calculateReadabilityScores(String description) {
        if (description.isEmpty()) {
            return new double[]{0, 0};
        }

        String[] words = splitIntoWords(description);
        int totalWords = words.length;
        int totalSentences = countSentences(description);

        int totalSyllables = 0;

        for (String word : words) {
            totalSyllables += countSyllables(word);
        }

        double wordsPerSentence = (double) totalWords / totalSentences;
        double syllablesPerWord = (double) totalSyllables / totalWords;
        double fkg = 0.39 * wordsPerSentence + 11.8 * syllablesPerWord - 15.59;
        double frs = 206.835 - 1.015 * wordsPerSentence - 84.6 * syllablesPerWord;
        return new double[]{fkg, frs};
    }

    /**
     * This method will calculate Syllables of the word.
     *
     * @param word work of description
     * @return count of syllables
     * @author Utsav Patel
     */
    public static int countSyllables(String word) {
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

        if (word.length() > 2) {
            if (word.endsWith("le")) {
                if (isConsonant(word.charAt(word.length() - 3))) {
                    syllableCount++;
                }
            }
        }

        if (word.endsWith("ed") && syllableCount > 1) {
            if (isConsonant(word.charAt(word.length() - 3))) {
                syllableCount--;
            }
        }

        if (word.endsWith("es") && syllableCount > 1) {
            if (isConsonant(word.charAt(word.length() - 3))) {
                syllableCount--;
            }
        }

        return Math.max(syllableCount, 1);
    }

    /**
     * This method will check whether it is consonant or not.
     *
     * @param c character
     * @return boolean
     * @author Utsav Patel
     */
    public static boolean isConsonant(char c) {
        return "bcdfghjklmnpqrstvwxyz".indexOf(c) >= 0;
    }

    /**
     * This method will split text into words
     *
     * @param text to split into words
     * @return Array of String
     * @author Utsav Patel
     */
    public static String[] splitIntoWords(String text) {
        return text.split("\\s+");
    }

    /**
     * This method will count number of sentences.
     *
     * @param text to count number of sentences
     * @return int - number of sentences
     * @author Utsav Patel
     */
    public static int countSentences(String text) {
        String[] sentences = text.split("[.!?;:]");
        return sentences.length;
    }

    /**
     * This method will fetch the videos from the YouTube API based on the provided keyword and then return the list of word and it's frequency from the all videos description .
     *
     * @author Karan Tanakhia
     */
    @Override
    public CompletionStage<List<String>> wordStatesVideos(String keyword) {

        CompletionStage<Response> responseCompletionStage = this.searchVideos(keyword);

        return responseCompletionStage.thenApply(response -> {
            List<Video> videos = response.getVideos();
            List<String> descriptions = videos.stream().map(Video::getDescription)
                    .collect(Collectors.toList());

            List<String> wordStats = calculateWordStats(descriptions);

            return wordStats;

        });

    }

    /**
     * This method will generate list string which contains the unique words and it's frequency form all the videos description and sort according to the descending order.
     *
     * @author Karan Tanakhia
     */
    private List<String> calculateWordStats(List<String> descriptions) {
        Map<String, Long> wordCounts = descriptions.stream()
                .flatMap(description -> Stream.of(description.split("\\W+")))
                .map(String::toLowerCase)
                .filter(word -> !word.isEmpty())
                .collect(Collectors.groupingBy(word -> word, Collectors.counting()));


        return wordCounts.entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.toList());
    }
}
