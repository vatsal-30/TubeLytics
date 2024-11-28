package actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.Response;
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

public class TaggedServiceActorTest {

    private static ActorSystem system;
    private static final String API_KEY = "test_api_key";
    private static final String TAG = "test_tag";

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
    public void testOnSearchByTag() {
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
            ArrayNode items = jsonResponse.putArray("items");
            ObjectNode item = items.addObject();
            ObjectNode id = item.putObject("id");
            id.put("videoId", "video_id_1");
            ObjectNode snippet = item.putObject("snippet");
            snippet.put("title", "Video Title 1");
            snippet.put("description", "Description 1");
            snippet.putObject("thumbnails").putObject("high").put("url", "http://example.com/thumbnail1.jpg");
            snippet.put("channelId", "channel_id_1");
            snippet.put("channelTitle", "Channel Title 1");

            when(wsResponse.asJson()).thenReturn(jsonResponse);

            // Create the actor
            final Props props = TaggedServiceActor.props(wsClient, API_KEY);
            final ActorRef taggedServiceActor = system.actorOf(props);

            // Send the message to the actor
            taggedServiceActor.tell(TAG, getRef());

            // Wait for the response
            Response response = expectMsgClass(Duration.ofSeconds(5), Response.class);

            // Verify the response
            assertEquals(TAG, response.getQuery());
            assertEquals(1, response.getVideos().size());

            Video video = response.getVideos().get(0);
            assertEquals("video_id_1", video.getVideoId());
            assertEquals("Video Title 1", video.getTitle());
            assertEquals("Description 1", video.getDescription());
            assertEquals("http://example.com/thumbnail1.jpg", video.getImageUrl());
            assertEquals("channel_id_1", video.getChannelId());
            assertEquals("Channel Title 1", video.getChannelTitle());
        }};
    }

    @Test
    public void testOnSearchByTagError() {
        new TestKit(system) {{
            // Mock WSClient and its dependencies
            WSClient wsClient = Mockito.mock(WSClient.class);
            WSRequest wsRequest = Mockito.mock(WSRequest.class);

            // Setup mock behaviors to simulate an error
            when(wsClient.url(anyString())).thenReturn(wsRequest);
            when(wsRequest.addQueryParameter(anyString(), anyString())).thenReturn(wsRequest);
            when(wsRequest.get()).thenReturn(CompletableFuture.failedFuture(new RuntimeException("API Error")));

            // Create the actor
            final Props props = TaggedServiceActor.props(wsClient, API_KEY);
            final ActorRef taggedServiceActor = system.actorOf(props);

            // Send the message to the actor
            taggedServiceActor.tell(TAG, getRef());

            // Wait for the response
            String errorMessage = expectMsgClass(Duration.ofSeconds(5), String.class);

            // Verify just the error message part
            assertEquals("API Error", errorMessage.replace("java.lang.RuntimeException: ", ""));
        }};
    }
}