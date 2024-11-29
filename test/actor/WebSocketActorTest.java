package actor;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import play.libs.ws.WSClient;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebSocketActorTest {
    private ActorSystem system;
    private TestKit probe;
    private ActorRef supervisorActor;

    @Mock
    WSClient wsClient;

    @Before
    public void setUp() {
        system = ActorSystem.create("TestSystem");
        probe = new TestKit(system);
        supervisorActor = system.actorOf(SupervisorActor.props(wsClient, "test-api-key"));
    }

    @After
    public void tearDown() {
        TestKit.shutdownActorSystem(system);
    }

    @Test
    public void testWebSocketActorInitialization() {
        ActorRef supervisorActor = system.actorOf(SupervisorActor.props(wsClient, "dummy-api-key"));
        ActorRef webSocketActor = system.actorOf(WebSocketActor.props(supervisorActor, "test-id", probe.getRef()));

        Assert.assertNotNull(webSocketActor);
    }

    @Test
    public void testNewConnection() {
        ActorRef supervisorActor = system.actorOf(SupervisorActor.props(wsClient, "dummy-api-key"));
        ActorRef webSocketActor = system.actorOf(WebSocketActor.props(supervisorActor, "test-id", probe.getRef()));

        ActorRef newConnectionActor = probe.getRef();
        WebSocketActor.NewConnection newConnection = new WebSocketActor.NewConnection(newConnectionActor);

        webSocketActor.tell(newConnection, ActorRef.noSender());
    }

    @Test
    public void testDisconnected() {
        ActorRef webSocketActor = system.actorOf(WebSocketActor.props(supervisorActor, "test-id", probe.getRef()));
        webSocketActor.tell(new WebSocketActor.Disconnected(probe.getRef()), probe.getRef());
    }

    @Test
    public void testActorSelectionFails() {
        ActorRef webSocketActor = system.actorOf(WebSocketActor.props(supervisorActor, "test-id", probe.getRef()));

        String message = "Test message";
        webSocketActor.tell(message, probe.getRef());

        ActorSelection mockActorSelection = mock(ActorSelection.class);
        when(mockActorSelection.resolveOne(Duration.ofSeconds(2))).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Actor resolution failed")));
        webSocketActor.tell(message, probe.getRef());
    }

    @Test
    public void testActorSelectionTimeout() {
        ActorRef webSocketActor = system.actorOf(WebSocketActor.props(supervisorActor, "test-id", probe.getRef()));

        ActorSelection mockActorSelection = mock(ActorSelection.class);
        when(mockActorSelection.resolveOne(Duration.ofSeconds(2)))
                .thenReturn(CompletableFuture.supplyAsync(() -> probe.getRef()));

        String message = "Test message";
        webSocketActor.tell(message, probe.getRef());
    }
}
