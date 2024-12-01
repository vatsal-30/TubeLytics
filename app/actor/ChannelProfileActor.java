package actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import model.ChannelProfile;
import model.Video;
import play.libs.ws.WSClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Actor responsible for fetching the Channel Profile and its videos using YouTube API.
 *
 * This actor communicates asynchronously to fetch the details of a YouTube channel and its recent videos.
 * It uses the WSClient to make API requests.
 *
 * @author Amish Navadia
 */

public class ChannelProfileActor extends AbstractActor {

    private final WSClient wsClient;
    private final String apiKey;
    private static final String YOUTUBE_CHANNEL_URL = "https://www.googleapis.com/youtube/v3/channels";
    private static final String YOUTUBE_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search";

    /**
     * Creates Props for the ChannelProfileActor.
     *
     * @param wsClient the WSClient used for making HTTP requests.
     * @param apiKey   the YouTube API key.
     * @return Props instance for creating the actor.
     * @see Props
     * @see WSClient
     * @author Amish Navadia
     */

    public static Props props(WSClient wsClient, String apiKey) {
        return Props.create(ChannelProfileActor.class, () -> new ChannelProfileActor(wsClient, apiKey));
    }

    /**
     * Constructor for ChannelProfileActor.
     *
     * @param wsClient the WSClient used for making HTTP requests.
     * @param apiKey   the YouTube API key.
     * @see WSClient
     * @see AbstractActor
     * @see ChannelProfileActor#props(WSClient, String)
     * @author Amish Navadia
     */

    public ChannelProfileActor(WSClient wsClient, String apiKey) {
        this.wsClient = wsClient;
        this.apiKey = apiKey;
    }

    /**
     * Defines the message-handling behavior for this actor.
     *
     * @return Receive object defining the actor's behavior.
     * @see AbstractActor#createReceive()
     * @see Channel
     * @see ChannelProfile
     * @see Video
     * @see WSClient
     * @author Amish Navadia
     */

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(Channel.class, this::handleFetchChannelProfile)
                .build();
    }

    /**
     * Handles a message to fetch the channel profile and its recent videos.
     *
     * This method fetches channel details and the recent videos for the given channel ID and sends
     * the resulting {@link ChannelProfile} object back to the sender.
     *
     * @param channel the message containing the channel ID.
     * @see Channel
     * @see ChannelProfile
     * @see Video
     * @see WSClient
     * @see CompletableFuture
     * @see CompletionStage
     * @author Amish Navadia
     */

    private void handleFetchChannelProfile(Channel channel) {
        ActorRef originalSender = getSender();

        // Fetch the channel profile
        CompletionStage<ChannelProfile> profileFuture = fetchChannelProfile(channel.channelId)
                .thenCompose(profile -> fetchChannelVideos(channel.channelId, 10)
                        .thenApply(videos -> {
                            profile.setVideos(videos);
                            return profile;
                        }));

        // Handle the result and send back the channel profile
        profileFuture.thenAccept(channelProfile -> {
            // Send the channel profile back to the original sender
            originalSender.tell(channelProfile, getSelf());
        }).exceptionally(ex -> {
            // Handle any exceptions and send an error message or null
            originalSender.tell(ex.getMessage(), getSelf());
            return null; // Return null to satisfy the exceptionally method
        });
    }


    /**
     * Fetches the profile details of a YouTube channel.
     *
     * @param channelId the ID of the channel to fetch the profile for.
     * @return a CompletionStage containing the ChannelProfile of the channel.
     * @see ChannelProfile
     * @see CompletableFuture
     * @see CompletionStage
     * @see WSClient
     * @see ArrayList
     * @author Amish Navadia
     */

    private CompletionStage<ChannelProfile> fetchChannelProfile(String channelId) {
        return wsClient.url(YOUTUBE_CHANNEL_URL)
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

                    return new ChannelProfile(name, imageUrl, description, subscriberCount, videoCount, new ArrayList<>());
                });
    }


    /**
     * Fetches the recent videos of a YouTube channel.
     *
     * @param channelId the ID of the channel to fetch videos for.
     * @param count     the maximum number of videos to fetch.
     * @return a CompletionStage containing a list of Video objects representing the channel's videos.
     * @see Video
     * @see CompletableFuture
     * @see ArrayList
     * @see WSClient
     * @see CompletionStage
     * @see ChannelProfileActor#fetchChannelProfile(String)
     * @author Amish Navadia
     */

    private CompletionStage<List<Video>> fetchChannelVideos(String channelId, int count) {
        return wsClient.url(YOUTUBE_SEARCH_URL)
                .addQueryParameter("part", "snippet")
                .addQueryParameter("channelId", channelId)
                .addQueryParameter("order", "date")
                .addQueryParameter("type", "video")
                .addQueryParameter("maxResults", Integer.toString(count))
                .addQueryParameter("key", apiKey)
                .get()
                .thenApply(response -> {
                    Iterable<com.fasterxml.jackson.databind.JsonNode> items = response.asJson().get("items");
                    List<Video> videos = new ArrayList<>();

                    items.forEach(item -> {
                        String videoId = item.get("id").get("videoId").asText();
                        String title = item.get("snippet").get("title").asText();
                        String description = item.get("snippet").get("description").asText();
                        String imageUrl = item.get("snippet").get("thumbnails").get("high").get("url").asText();
                        String channelTitle = item.get("snippet").get("channelTitle").asText();
                        videos.add(new Video(videoId, title, description, imageUrl, channelId, channelTitle));
                    });

                    return videos;
                });

    }


    /**
     * Message class representing a request to fetch a channel profile.
     */

    public static class Channel {
        public final String channelId;

        /**
         * Constructor for the Channel message.
         *
         * @param channelId the ID of the channel to fetch the profile for.
         * @see ChannelProfileActor
         * @see ChannelProfile
         * @see Video
         * @see WSClient
         * @see ArrayList
         * @see CompletableFuture
         * @author Amish Navadia
         */

        public Channel(String channelId) {
            this.channelId = channelId;
        }
    }
}
