import actor.SupervisorActor;
import actor.UserActor;
import actor.WebSocketActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import controllers.YouTubeController;
import model.Response;
import model.Video;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.shaded.ahc.org.asynchttpclient.ws.WebSocketListener;
import org.junit.Assert;
import org.junit.Test;
import play.libs.Json;
import play.shaded.ahc.org.asynchttpclient.*;
import play.shaded.ahc.org.asynchttpclient.netty.ws.NettyWebSocket;
import play.shaded.ahc.org.asynchttpclient.ws.WebSocketUpgradeHandler;
import play.test.TestServer;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static org.awaitility.Awaitility.await;

import play.shaded.ahc.org.asynchttpclient.ws.WebSocket;
import services.YouTubeService;
import services.impl.YouTubeServiceImpl;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.test.Helpers.*;

public class WebSocketTest {
    private Response response;
    private final ObjectMapper mapper = new ObjectMapper();

    private ActorSystem system;
    private ActorRef webSocketActor;

    @Mock
    private Config config;

    @Mock
    private WSClient wsClient;

    @Mock
    private WSRequest wsRequest;

    @Mock
    private WSResponse wsResponse;

    @Mock
    private UserActor userActor;  // Mock UserActor

    @Mock
    private SupervisorActor supervisorActor;  // Mock SupervisorActor

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        response = new Response();
        response.setQuery("Java");
        response.setVideos(new ArrayList<>());

        Video video = new Video("vid-001", "Understanding the Basics of Reading Levels", "An introduction to understanding reading levels and their impact on learning", "https://example.com/image1.jpg", "channel-001", "Education Today");
        response.getVideos().add(video);

        video = new Video("vid-002", "Tips for Improving Reading Comprehension", "Tips for Improving Reading Comprehension", "https://example.com/image2.jpg", "channel-002", "Learning Insights");
        response.getVideos().add(video);

        video = new Video("vid-003", "Advanced Strategies for Teaching Reading Skills", "Discover advanced strategies for teaching reading skills effectively.", "https://example.com/image3.jpg", "channel-003", "Teacher's Hub");
        response.getVideos().add(video);

        video = new Video("vid-004", "The Importance of Reading for Young Learners", "Explores why reading is essential for early childhood education.", "https://example.com/image4.jpg", "channel-004", "Kids Academy");
        response.getVideos().add(video);

        video = new Video("vid-005", "How to Foster a Love for Reading", "Ways to encourage a lifelong passion for reading in children.", "https://example.com/image5.jpg", "channel-005", "Book Lovers");
        response.getVideos().add(video);

        when(config.getString("youtube.api.key")).thenReturn("API_KEY");
        when(wsClient.url(anyString())).thenReturn(wsRequest);
        when(wsRequest.addQueryParameter(anyString(), anyString())).thenReturn(wsRequest);
        when(wsRequest.get()).thenReturn(CompletableFuture.completedFuture(wsResponse));
        when(wsResponse.asJson()).thenReturn(Json.toJson(response));

        // Mock the UserActor's searchVideos method to return a future with the mock response
        when(userActor.searchVideos(anyString())).thenReturn(CompletableFuture.completedFuture(response));
    }

//    @Test
    public void testWebSocket() {
        TestServer server = testServer(19001, fakeApplication());
        running(server, () -> {
            try {
                AsyncHttpClientConfig config = new DefaultAsyncHttpClientConfig.Builder().setMaxRequestRetry(0).build();
                AsyncHttpClient client = new DefaultAsyncHttpClient(config);

                try (client) {
                    WebSocketClient webSocketClient = new WebSocketClient(client);
                    String serverURL = "ws://localhost:19001/ws";
                    ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
                    WebSocketClient.LoggingListener listener = new WebSocketClient.LoggingListener((message) -> {
                        try {
                            System.out.println("Message is : " + message);
                            queue.put(message);
                        } catch (InterruptedException e) {
                            System.out.println("Unexpected exception : " + e.getMessage());
                        }
                    });
                    CompletableFuture<NettyWebSocket> completionStage = webSocketClient.call(serverURL, serverURL, listener);

                    await().until(completionStage::isDone);
                    WebSocket websocket = completionStage.get();

                    Assert.assertTrue(websocket.isOpen());
//                    YouTubeService youTubeService1 = mock(YouTubeService.class);
                    when(userActor.searchVideos(anyString())).thenReturn(CompletableFuture.completedFuture(response));
                    websocket.sendTextFrame("Java");
                    await().until(() -> websocket.isOpen() && queue.peek() != null);
                    String input = queue.take();

                    JsonNode json = Json.parse(input);

                    Response responseFromSocket = mapper.treeToValue(json, Response.class);
                    Assert.assertNotNull(responseFromSocket);
                    Assert.assertEquals("Java", responseFromSocket.getQuery());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    static class WebSocketClient {
        private final AsyncHttpClient client;

        public WebSocketClient(AsyncHttpClient c) {
            this.client = c;
        }

        public CompletableFuture<NettyWebSocket> call(String url, String origin, WebSocketListener listener) throws ExecutionException, InterruptedException {
            final BoundRequestBuilder requestBuilder = client.prepareGet(url).addHeader("Origin", origin);

            final WebSocketUpgradeHandler handler = new WebSocketUpgradeHandler.Builder().addWebSocketListener(listener).build();
            final ListenableFuture<NettyWebSocket> future = requestBuilder.execute(handler);
            return future.toCompletableFuture();
        }

        static class LoggingListener implements WebSocketListener {
            private final Consumer<String> onMessageCallback;

            public LoggingListener(Consumer<String> onMessageCallback) {
                this.onMessageCallback = onMessageCallback;
            }

            private Throwable throwableFound = null;

            public Throwable getThrowable() {
                return throwableFound;
            }

            public void onOpen(WebSocket websocket) {
            }

            @Override
            public void onClose(WebSocket webSocket, int i, String s) {
            }

            public void onError(Throwable t) {
                throwableFound = t;
            }

            @Override
            public void onTextFrame(String payload, boolean finalFragment, int rsv) {
                onMessageCallback.accept(payload);
            }
        }
    }
}
