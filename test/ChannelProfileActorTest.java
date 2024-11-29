
import actor.ChannelProfileActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.ChannelProfile;
import model.Video;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ChannelProfileActorTest {

    private static ActorSystem system;
    private static final String API_KEY = "test_api_key";
    private static final String CHANNEL_ID = "test_channel_id";


    private static final String YOUTUBE_CHANNEL_URL = "https://www.googleapis.com/youtube/v3/channels";
    private static final String YOUTUBE_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search";

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testFetchChannelProfileSuccess() {
        new TestKit(system) {{
            // Mock WSClient and its dependencies
            WSClient wsClient = Mockito.mock(WSClient.class);
            WSRequest profileRequest = Mockito.mock(WSRequest.class);
            WSRequest videosRequest = Mockito.mock(WSRequest.class);
            WSResponse profileResponse = Mockito.mock(WSResponse.class);
            WSResponse videosResponse = Mockito.mock(WSResponse.class);

            // Mock the profile API response
            when(wsClient.url(YOUTUBE_CHANNEL_URL)).thenReturn(profileRequest);
            when(profileRequest.addQueryParameter(anyString(), anyString())).thenReturn(profileRequest);
            when(profileRequest.get()).thenReturn(CompletableFuture.completedFuture(profileResponse));

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode profileJson = mapper.createObjectNode();
            ArrayNode profileItems = profileJson.putArray("items");
            ObjectNode profileItem = profileItems.addObject();
            ObjectNode snippet = profileItem.putObject("snippet");
            snippet.put("title", "Test Channel");
            snippet.putObject("thumbnails").putObject("default").put("url", "http://example.com/channel-thumbnail.jpg");
            snippet.put("description", "Test Description");
            ObjectNode statistics = profileItem.putObject("statistics");
            statistics.put("subscriberCount", "1000");
            statistics.put("videoCount", "50");
            when(profileResponse.asJson()).thenReturn(profileJson);

            // Mock the videos API response
            when(wsClient.url(YOUTUBE_SEARCH_URL)).thenReturn(videosRequest);
            when(videosRequest.addQueryParameter(anyString(), anyString())).thenReturn(videosRequest);
            when(videosRequest.get()).thenReturn(CompletableFuture.completedFuture(videosResponse));

            ObjectNode videosJson = mapper.createObjectNode();
            ArrayNode videoItems = videosJson.putArray("items");
            ObjectNode videoItem = videoItems.addObject();
            videoItem.putObject("id").put("videoId", "video_id_1");
            ObjectNode videoSnippet = videoItem.putObject("snippet");
            videoSnippet.put("title", "Video Title 1");
            videoSnippet.put("description", "Video Description 1");
            videoSnippet.putObject("thumbnails").putObject("high").put("url", "http://example.com/video-thumbnail.jpg");
            videoSnippet.put("channelTitle", "Test Channel");
            when(videosResponse.asJson()).thenReturn(videosJson);

            // Create the actor
            final Props props = ChannelProfileActor.props(wsClient, API_KEY);
            final ActorRef channelProfileActor = system.actorOf(props);

            // Send the message to the actor
            ChannelProfileActor.Channel channelMessage = new ChannelProfileActor.Channel(CHANNEL_ID);
            channelProfileActor.tell(channelMessage, getRef());

            // Wait for the response
            ChannelProfile profile = expectMsgClass(Duration.ofSeconds(5), ChannelProfile.class);

            // Verify the response
            assertNotNull(profile);
            assertEquals("Test Channel", profile.getName());
            assertEquals("http://example.com/channel-thumbnail.jpg", profile.getImageUrl());
            assertEquals("Test Description", profile.getDescription());
            assertEquals("1000", profile.getSubscriberCount());
            assertEquals("50", profile.getVideoCount());
            assertEquals(1, profile.getVideos().size());
            Video video = profile.getVideos().get(0);
            assertEquals("video_id_1", video.getVideoId());
            assertEquals("Video Title 1", video.getTitle());
            assertEquals("Video Description 1", video.getDescription());
        }};
    }

    @Test
    public void testFetchChannelProfileFailure() {
        new TestKit(system) {{
            // Mock WSClient and its dependencies
            WSClient wsClient = Mockito.mock(WSClient.class);
            WSRequest profileRequest = Mockito.mock(WSRequest.class);

            // Simulate a failure in fetching channel profile
            when(wsClient.url(YOUTUBE_CHANNEL_URL)).thenReturn(profileRequest);
            when(profileRequest.addQueryParameter(anyString(), anyString())).thenReturn(profileRequest);
            when(profileRequest.get()).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Profile API Error")));

            // Create the actor
            final Props props = ChannelProfileActor.props(wsClient, API_KEY);
            final ActorRef channelProfileActor = system.actorOf(props);

            // Send the message to the actor
            ChannelProfileActor.Channel channelMessage = new ChannelProfileActor.Channel(CHANNEL_ID);
            channelProfileActor.tell(channelMessage, getRef());

            // Wait for the response
            String errorMessage = expectMsgClass(Duration.ofSeconds(5), String.class);

            // Verify the error message
            assertEquals("Profile API Error", errorMessage.replace("java.lang.RuntimeException: ", ""));
        }};
    }

    @Test
    public void testFetchChannelVideosFailure() {
        new TestKit(system) {{
            // Mock WSClient and its dependencies
            WSClient wsClient = Mockito.mock(WSClient.class);
            WSRequest profileRequest = Mockito.mock(WSRequest.class);
            WSRequest videosRequest = Mockito.mock(WSRequest.class);
            WSResponse profileResponse = Mockito.mock(WSResponse.class);
            WSResponse videosResponse = Mockito.mock(WSResponse.class);

            // Mock the profile API response (Similar to the previous success case)
            when(wsClient.url(YOUTUBE_CHANNEL_URL)).thenReturn(profileRequest);
            when(profileRequest.addQueryParameter(anyString(), anyString())).thenReturn(profileRequest);
            when(profileRequest.get()).thenReturn(CompletableFuture.completedFuture(profileResponse));

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode profileJson = mapper.createObjectNode();
            ArrayNode profileItems = profileJson.putArray("items");
            ObjectNode profileItem = profileItems.addObject();
            ObjectNode snippet = profileItem.putObject("snippet");
            snippet.put("title", "Test Channel");
            snippet.putObject("thumbnails").putObject("default").put("url", "http://example.com/channel-thumbnail.jpg");
            snippet.put("description", "Test Description");
            ObjectNode statistics = profileItem.putObject("statistics");
            statistics.put("subscriberCount", "1000");
            statistics.put("videoCount", "50");
            when(profileResponse.asJson()).thenReturn(profileJson);

            // Simulate a failure in fetching videos
            when(wsClient.url(YOUTUBE_SEARCH_URL)).thenReturn(videosRequest);
            when(videosRequest.addQueryParameter(anyString(), anyString())).thenReturn(videosRequest);
            when(videosRequest.get()).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Videos API Error")));

            // Create the actor
            final Props props = ChannelProfileActor.props(wsClient, API_KEY);
            final ActorRef channelProfileActor = system.actorOf(props);

            // Send the message to the actor
            ChannelProfileActor.Channel channelMessage = new ChannelProfileActor.Channel(CHANNEL_ID);
            channelProfileActor.tell(channelMessage, getRef());

            // Wait for the response
            String errorMessage = expectMsgClass(Duration.ofSeconds(5), String.class);

            // Verify the error message
            assertEquals("Videos API Error", errorMessage.replace("java.lang.RuntimeException: ", ""));
        }};
    }


}
