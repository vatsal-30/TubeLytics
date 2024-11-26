package actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class VideoServiceActorTest {

    private static ActorSystem system;
    private static final String API_KEY = "test_api_key";
    private static final String VIDEO_ID = "test_video_id";

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
    public void testFetchVideoDetails() {
        new TestKit(system) {{
            // Mock WSClient and its dependencies
            WSClient wsClient = Mockito.mock(WSClient.class);
            WSRequest wsRequest = Mockito.mock(WSRequest.class);
            WSResponse wsResponse = Mockito.mock(WSResponse.class);

            // Setup mock behaviors
            when(wsClient.url(anyString())).thenReturn(wsRequest);
            when(wsRequest.addQueryParameter(anyString(), anyString())).thenReturn(wsRequest);
            when(wsRequest.get()).thenReturn(CompletableFuture.completedFuture(wsResponse));

            // Create mock JSON response
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jsonResponse = mapper.createObjectNode();
            ObjectNode items = jsonResponse.putArray("items").addObject();
            ObjectNode snippet = items.putObject("snippet");
            snippet.put("title", "Test Video Title");
            snippet.put("description", "Test video description");
            snippet.putObject("thumbnails").putObject("high").put("url", "http://example.com/thumbnail.jpg");
            snippet.put("channelId", "test_channel_id");
            snippet.put("channelTitle", "Test Channel");
            snippet.putArray("tags").add("tag1").add("tag2");

            when(wsResponse.asJson()).thenReturn(jsonResponse);

            // Create the actor
            final Props props = VideoServiceActor.props(wsClient, API_KEY);
            final ActorRef videoServiceActor = system.actorOf(props);

            // Send the message to the actor
            videoServiceActor.tell(VIDEO_ID, getRef());

            // Wait for the response
            Video response = expectMsgClass(Duration.ofSeconds(5), Video.class);

            // Verify the response
            assertEquals(VIDEO_ID, response.getVideoId());
            assertEquals("Test Video Title", response.getTitle());
            assertEquals("Test video description", response.getDescription());
            assertEquals("http://example.com/thumbnail.jpg", response.getImageUrl());
            assertEquals("test_channel_id", response.getChannelId());
            assertEquals("Test Channel", response.getChannelTitle());
            assertEquals("tag1,tag2", response.getTags());
        }};
    }

    @Test
    public void testFetchVideoDetailsError() {
        new TestKit(system) {{
            // Mock WSClient and its dependencies
            WSClient wsClient = Mockito.mock(WSClient.class);
            WSRequest wsRequest = Mockito.mock(WSRequest.class);

            // Setup mock behaviors to simulate an error
            when(wsClient.url(anyString())).thenReturn(wsRequest);
            when(wsRequest.addQueryParameter(anyString(), anyString())).thenReturn(wsRequest);
            when(wsRequest.get()).thenReturn(CompletableFuture.failedFuture(new RuntimeException("API Error")));

            // Create the actor
            final Props props = VideoServiceActor.props(wsClient, API_KEY);
            final ActorRef videoServiceActor = system.actorOf(props);

            // Send the message to the actor
            videoServiceActor.tell(VIDEO_ID, getRef());

            // Wait for the response
            String errorMessage = expectMsgClass(Duration.ofSeconds(5), String.class);

            // Verify the error message
            assertEquals("java.lang.RuntimeException: API Error", errorMessage);
        }};
    }
}