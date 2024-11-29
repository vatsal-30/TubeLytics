package actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Response;
import model.Video;
import org.mockito.MockedConstruction;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class UserActorTest {

    private static ActorSystem actorSystem;
    private static final String API_KEY = "test_api_key";
    private static final String SEARCH_KEYWORD = "test_keyword";
    private String jsonResponse;

    @Before
    public void setUp() {
        jsonResponse = """
                {
                    "items": [
                        {
                            "id": {
                                "videoId": "vid-001"
                            },
                            "snippet": {
                                "title": "Understanding the Basics of Reading Levels",
                                "description": "An introduction to understanding reading levels and their impact on learning",
                                "thumbnails": {
                                    "high": {
                                        "url": "https://example.com/image1.jpg"
                                    }
                                },
                                "channelId": "channel-001",
                                "channelTitle": "Education Today"
                            }
                        },
                        {
                            "id": {
                                "videoId": "vid-002"
                            },
                            "snippet": {
                                "title": "Tips for Improving Reading Comprehension",
                                "description": "Tips for Improving Reading Comprehension.",
                                "thumbnails": {
                                    "high": {
                                        "url": "https://example.com/image2.jpg"
                                    }
                                },
                                "channelId": "channel-002",
                                "channelTitle": "Learning Insights"
                            }
                        }
                    ]
                }
                """;
    }

    @BeforeClass
    public static void setup() {
        actorSystem = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(actorSystem);
        actorSystem = null;
    }

    @Test
    public void testOnSearchVideos() throws JsonProcessingException {
        WSClient wsClient = mock(WSClient.class);
        WSRequest wsRequest = mock(WSRequest.class);
        WSResponse wsResponse = mock(WSResponse.class);

        ObjectMapper mapper = new ObjectMapper();

        when(wsClient.url(anyString())).thenReturn(wsRequest);
        when(wsRequest.addQueryParameter(anyString(), anyString())).thenReturn(wsRequest);
        when(wsRequest.get()).thenReturn(CompletableFuture.completedFuture(wsResponse));
        when(wsResponse.asJson()).thenReturn(mapper.readTree(jsonResponse));

        final TestKit probe = new TestKit(actorSystem);

        final ActorRef userActor = actorSystem.actorOf(UserActor.props(probe.getRef(), wsClient, API_KEY), "UserActor");

        userActor.tell(SEARCH_KEYWORD, probe.getRef());

        String responseWithReadabilityScore = probe.expectMsgClass(Duration.ofSeconds(5), String.class);

        Response response = mapper.readValue(responseWithReadabilityScore, Response.class);

        assertEquals(SEARCH_KEYWORD, response.getQuery());
        assertEquals(2, response.getVideos().size());

        Video video1 = response.getVideos().get(0);
        assertEquals("vid-001", video1.getVideoId());
        assertEquals("Understanding the Basics of Reading Levels", video1.getTitle());
        assertEquals("https://example.com/image1.jpg", video1.getImageUrl());
    }

    @Test
    public void testOnEmptyApiResponse() throws JsonProcessingException {
        WSClient wsClient = mock(WSClient.class);
        WSRequest wsRequest = mock(WSRequest.class);
        WSResponse wsResponse = mock(WSResponse.class);

        ObjectMapper mapper = new ObjectMapper();
        String emptyJsonResponse = "{ \"items\": [] }";

        when(wsClient.url(anyString())).thenReturn(wsRequest);
        when(wsRequest.addQueryParameter(anyString(), anyString())).thenReturn(wsRequest);
        when(wsRequest.get()).thenReturn(CompletableFuture.completedFuture(wsResponse));
        when(wsResponse.asJson()).thenReturn(mapper.readTree(emptyJsonResponse));

        final TestKit probe = new TestKit(actorSystem);
        final ActorRef userActor = actorSystem.actorOf(UserActor.props(probe.getRef(), wsClient, API_KEY), "UserActorEmptyResponse");

        userActor.tell(SEARCH_KEYWORD, probe.getRef());

        String responseWithReadabilityScore = probe.expectMsgClass(Duration.ofSeconds(5), String.class);
        Response response = mapper.readValue(responseWithReadabilityScore, Response.class);

        assertEquals(SEARCH_KEYWORD, response.getQuery());
        assertEquals(0, response.getVideos().size());
    }

    @Test(expected = RuntimeException.class)
    public void testSerializeResponseFailure() {
        try (MockedConstruction<ObjectMapper> ignored = mockConstruction(ObjectMapper.class,
                (mock, context) -> {
                    when(mock.writeValueAsString(any(Response.class)))
                            .thenThrow(new JsonProcessingException("Mocked Serialization Error") {
                            });
                })) {
            Response response = new Response();
            UserActor.serializeResponse(response);
        }
    }
}