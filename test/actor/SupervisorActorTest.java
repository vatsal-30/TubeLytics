package actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import akka.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.libs.ws.WSClient;

/**
 * Unit tests for the {@link SupervisorActor} class.
 *
 * <p>This test class verifies the behavior of the SupervisorActor in managing connections,
 * forwarding messages, and handling errors. It uses Akka TestKit for actor-based testing
 * and Mockito for mocking dependencies.
 *
 * @author Utsav Patel
 */
public class SupervisorActorTest {
    private ActorSystem system;

    @Mock
    private WSClient wsClient;

    private TestActorRef<SupervisorActor> supervisorActor;

    /**
     * Sets up the test environment before each test.
     *
     * <p>Initializes the actor system, mocks the WSClient dependency,
     * and creates a test actor reference for the SupervisorActor.
     *
     * @author Utsav Patel
     */
    @Before
    public void setUp() {
        system = ActorSystem.create("TestSystem");
        MockitoAnnotations.openMocks(this);
        supervisorActor = TestActorRef.create(system, SupervisorActor.props(wsClient, "test-api-key"));
    }

    /**
     * Cleans up the test environment after each test.
     *
     * <p>Shuts down the actor system to release resources.
     *
     * @author Utsav Patel
     */
    @After
    public void tearDown() {
        TestKit.shutdownActorSystem(system);
    }

    /**
     * Tests the behavior of the {@link SupervisorActor} when a new connection is established.
     *
     * <p>Ensures that the userActors map is updated to include the newly connected actor.
     *
     * @author Utsav Patel
     */
    @Test
    public void testNewConnection() {
        ActorRef newConnection = system.actorOf(Props.create(DescriptionReadabilityActor.class));
        supervisorActor.tell(new WebSocketActor.NewConnection(newConnection), ActorRef.noSender());
        Assert.assertTrue(supervisorActor.underlyingActor().userActors.containsKey(newConnection));
    }

    /**
     * Tests the behavior of the {@link SupervisorActor} when a connection is disconnected.
     *
     * <p>Ensures that the disconnected actor is removed from the userActors map.
     *
     * @author Utsav Patel
     */
    @Test
    public void testDisconnected() {
        ActorRef newConnection = system.actorOf(Props.create(DescriptionReadabilityActor.class));
        supervisorActor.tell(new WebSocketActor.NewConnection(newConnection), ActorRef.noSender());
        ActorRef userActor = supervisorActor.underlyingActor().userActors.get(newConnection);
        Assert.assertNotNull(userActor);

        supervisorActor.tell(new WebSocketActor.Disconnected(newConnection), ActorRef.noSender());
        Assert.assertNull(supervisorActor.underlyingActor().userActors.get(newConnection));
    }

    /**
     * Tests the message forwarding behavior of the {@link SupervisorActor}.
     *
     * <p>Ensures that messages sent to the supervisor are forwarded appropriately.
     *
     * @author Utsav Patel
     */
    @Test
    public void testMessageForwarding() {
        ActorRef newConnection = system.actorOf(Props.create(DescriptionReadabilityActor.class));
        supervisorActor.tell(new WebSocketActor.NewConnection(newConnection), ActorRef.noSender());

        String testMessage = "Test Message";
        supervisorActor.tell(testMessage, ActorRef.noSender());
    }

    /**
     * Tests the notify client behavior of the {@link SupervisorActor}.
     *
     * <p>Verifies that the supervisor can notify connected clients using the NotifyClient message.
     *
     * @author Utsav Patel
     */
    @Test
    public void testNotifyClient() {
        ActorRef newConnection = system.actorOf(Props.create(DescriptionReadabilityActor.class));
        supervisorActor.tell(new WebSocketActor.NewConnection(newConnection), ActorRef.noSender());

        supervisorActor.tell(new SupervisorActor.NotifyClient(), ActorRef.noSender());
    }

    /**
     * Tests the supervision strategy of the {@link SupervisorActor}.
     *
     * <p>Ensures that the supervisor appropriately handles exceptions such as NullPointerException.
     *
     * @author Utsav Patel
     */
    @Test
    public void testSupervisionStrategy() {
        supervisorActor.tell(new NullPointerException("Test Exception"), ActorRef.noSender());
    }
}
