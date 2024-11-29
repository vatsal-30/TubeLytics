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

public class SupervisorActorTest {
    private ActorSystem system;

    @Mock
    private WSClient wsClient;

    private TestActorRef<SupervisorActor> supervisorActor;

    @Before
    public void setUp() {
        system = ActorSystem.create("TestSystem");
        MockitoAnnotations.openMocks(this);
        supervisorActor = TestActorRef.create(system, SupervisorActor.props(wsClient, "test-api-key"));
    }

    @After
    public void tearDown() {
        TestKit.shutdownActorSystem(system);
    }

    @Test
    public void testNewConnection() {
        ActorRef newConnection = system.actorOf(Props.create(DescriptionReadabilityActor.class));
        supervisorActor.tell(new WebSocketActor.NewConnection(newConnection), ActorRef.noSender());
        Assert.assertTrue(supervisorActor.underlyingActor().userActors.containsKey(newConnection));
    }

    @Test
    public void testDisconnected() {
        ActorRef newConnection = system.actorOf(Props.create(DescriptionReadabilityActor.class));
        supervisorActor.tell(new WebSocketActor.NewConnection(newConnection), ActorRef.noSender());
        ActorRef userActor = supervisorActor.underlyingActor().userActors.get(newConnection);
        Assert.assertNotNull(userActor);

        supervisorActor.tell(new WebSocketActor.Disconnected(newConnection), ActorRef.noSender());
        Assert.assertNull(supervisorActor.underlyingActor().userActors.get(newConnection));
    }

    @Test
    public void testMessageForwarding() {
        ActorRef newConnection = system.actorOf(Props.create(DescriptionReadabilityActor.class));
        supervisorActor.tell(new WebSocketActor.NewConnection(newConnection), ActorRef.noSender());

        String testMessage = "Test Message";
        supervisorActor.tell(testMessage, ActorRef.noSender());
    }

    @Test
    public void testNotifyClient() {
        ActorRef newConnection = system.actorOf(Props.create(DescriptionReadabilityActor.class));
        supervisorActor.tell(new WebSocketActor.NewConnection(newConnection), ActorRef.noSender());

        supervisorActor.tell(new SupervisorActor.NotifyClient(), ActorRef.noSender());
    }

    @Test
    public void testSupervisionStrategy() {
        supervisorActor.tell(new NullPointerException("Test Exception"), ActorRef.noSender());
    }
}
