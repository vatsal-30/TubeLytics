package actor;

import akka.actor.*;

import java.time.Duration;

/**
 * The WebSocketActor class handles WebSocket connections for individual clients.
 *
 * <p>It communicates with a supervisor actor to manage connection lifecycles and forward
 * messages. This actor is responsible for handling the following actions:
 * <ul>
 *   <li>Notifying the supervisor about new connections.</li>
 *   <li>Notifying the supervisor when a client disconnects.</li>
 *   <li>Resolving and forwarding messages to appropriate WebSocket actors.</li>
 * </ul>
 *
 * @author Utsav Patel
 */
public class WebSocketActor extends AbstractActor {

    private final ActorRef supervisorRef;
    private final String id;
    private final ActorRef actorRef;

    /**
     * Represents a new connection message.
     *
     * @author Utsav Patel
     */
    public static class NewConnection {
        private final ActorRef actorRef;

        public NewConnection(ActorRef actorRef) {
            this.actorRef = actorRef;
        }

        public ActorRef getActorRef() {
            return actorRef;
        }
    }

    /**
     * Represents a disconnection message.
     *
     * @author Utsav Patel
     */
    public static class Disconnected {
        private final ActorRef actorRef;

        public Disconnected(ActorRef actorRef) {
            this.actorRef = actorRef;
        }

        public ActorRef getActorRef() {
            return actorRef;
        }
    }

    /**
     * Constructs a WebSocketActor.
     *
     * @param supervisorActorRef the supervisor actor reference
     * @param id                 the unique ID for this WebSocket actor
     * @param actorRef           the actor reference for this WebSocket
     * @author Utsav Patel
     */
    public WebSocketActor(ActorRef supervisorActorRef, String id, ActorRef actorRef) {
        this.supervisorRef = supervisorActorRef;
        this.id = id;
        this.actorRef = actorRef;
    }

    /**
     * Factory method to create a Props instance for this actor.
     *
     * @param supervisorActorRef the supervisor actor reference
     * @param id                 the unique ID for this WebSocket actor
     * @param actorRef           the actor reference for this WebSocket
     * @return a Props instance for creating this actor
     * @author Utsav Patel
     */
    public static Props props(ActorRef supervisorActorRef, String id, ActorRef actorRef) {
        return Props.create(WebSocketActor.class, () -> new WebSocketActor(supervisorActorRef, id, actorRef));
    }

    /**
     * Defines the behavior of the WebSocketActor.
     *
     * @return Receive instance defining message handling logic
     * @author Utsav Patel
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(NewConnection.class, newConnection -> {
                    supervisorRef.tell(newConnection, getSelf());
                })
                .match(Disconnected.class, disconnected -> {
                    supervisorRef.tell(disconnected, getSelf());
                })
                .match(String.class, message -> {
                    String actorPath = "akka://application/user/websocket-" + id;
                    ActorSelection actorSelection = context().actorSelection(actorPath);

                    actorSelection.resolveOne(Duration.ofSeconds(2)).toCompletableFuture()
                            .thenAccept(actorRef -> {
                                supervisorRef.tell(message, actorRef);
                            });
                })
                .build();
    }
}
