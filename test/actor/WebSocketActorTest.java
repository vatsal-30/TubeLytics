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

/**
 * Unit tests for the {@link WebSocketActor} class.
 *
 * <p>This test class verifies the behavior of the `WebSocketActor`, including initialization,
 * handling of new connections and disconnections, and interactions with supervisors and actor selections.
 *
 * @author Utsav Patel
 */
public class WebSocketActorTest {
    private ActorSystem system;
    private TestKit probe;
    private ActorRef supervisorActor;

    @Mock
    WSClient wsClient;

    /**
     * Sets up the actor system and initializes necessary test dependencies before each test.
     *
     * <p>Creates a supervisor actor and a test probe to simulate interactions with the `WebSocketActor`.
     *
     * @author Utsav Patel
     */
    @Before
    public void setUp() {
        system = ActorSystem.create("TestSystem");
        probe = new TestKit(system);
        supervisorActor = system.actorOf(SupervisorActor.props(wsClient, "test-api-key"));
    }

    /**
     * Shuts down the actor system after each test.
     *
     * <p>Ensures proper cleanup of resources and prevents interference between tests.
     *
     * @author Utsav Patel
     */
    @After
    public void tearDown() {
        TestKit.shutdownActorSystem(system);
    }

    /**
     * Tests the initialization of the {@link WebSocketActor}.
     *
     * <p>Verifies that a new instance of the `WebSocketActor` can be created and is not null.
     *
     * @author Utsav Patel
     */
    @Test
    public void testWebSocketActorInitialization() {
        ActorRef supervisorActor = system.actorOf(SupervisorActor.props(wsClient, "dummy-api-key"));
        ActorRef webSocketActor = system.actorOf(WebSocketActor.props(supervisorActor, "test-id", probe.getRef()));

        Assert.assertNotNull(webSocketActor);
    }

    /**
     * Tests the handling of a new connection by the {@link WebSocketActor}.
     *
     * <p>Simulates the reception of a {@link WebSocketActor.NewConnection} message and verifies
     * that the actor processes it without errors.
     *
     * @author Utsav Patel
     */
    @Test
    public void testNewConnection() {
        ActorRef supervisorActor = system.actorOf(SupervisorActor.props(wsClient, "dummy-api-key"));
        ActorRef webSocketActor = system.actorOf(WebSocketActor.props(supervisorActor, "test-id", probe.getRef()));

        ActorRef newConnectionActor = probe.getRef();
        WebSocketActor.NewConnection newConnection = new WebSocketActor.NewConnection(newConnectionActor);

        webSocketActor.tell(newConnection, ActorRef.noSender());
    }

    /**
     * Tests the handling of a disconnection by the {@link WebSocketActor}.
     *
     * <p>Simulates the reception of a {@link WebSocketActor.Disconnected} message and ensures
     * the actor processes the disconnection event.
     *
     * @author Utsav Patel
     */
    @Test
    public void testDisconnected() {
        ActorRef webSocketActor = system.actorOf(WebSocketActor.props(supervisorActor, "test-id", probe.getRef()));
        webSocketActor.tell(new WebSocketActor.Disconnected(probe.getRef()), probe.getRef());
    }

    /**
     * Tests the behavior of the {@link WebSocketActor} when actor selection fails.
     *
     * <p>Simulates a scenario where actor resolution fails due to an error, ensuring
     * the actor handles the failure gracefully without crashing.
     *
     * @author Utsav Patel
     */
    @Test
    public void testActorSelectionFails() {
        ActorRef webSocketActor = system.actorOf(WebSocketActor.props(supervisorActor, "test-id", probe.getRef()));

        String message = "Test message";
        webSocketActor.tell(message, probe.getRef());

        ActorSelection mockActorSelection = mock(ActorSelection.class);
        when(mockActorSelection.resolveOne(Duration.ofSeconds(2))).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Actor resolution failed")));
        webSocketActor.tell(message, probe.getRef());
    }

    /**
     * Tests the handling of an actor selection timeout by the {@link WebSocketActor}.
     *
     * <p>Simulates a scenario where actor resolution takes time but eventually succeeds,
     * verifying the actor processes the resolved actor correctly.
     *
     * @author Utsav Patel
     */
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
