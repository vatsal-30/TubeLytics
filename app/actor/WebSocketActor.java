package actor;

import akka.actor.*;

import java.time.Duration;

public class WebSocketActor extends AbstractActor {

    private final ActorRef supervisorRef;
    private final String id;
    private final ActorRef actorRef;

    public static class NewConnection {
        private final ActorRef actorRef;

        public NewConnection(ActorRef actorRef) {
            this.actorRef = actorRef;
        }

        public ActorRef getActorRef() {
            return actorRef;
        }
    }

    public static class Disconnected {
        private final ActorRef actorRef;

        public Disconnected(ActorRef actorRef) {
            this.actorRef = actorRef;
        }

        public ActorRef getActorRef() {
            return actorRef;
        }
    }

    public WebSocketActor(ActorRef supervisorActorRef, String id, ActorRef actorRef) {
        this.supervisorRef = supervisorActorRef;
        this.id = id;
        this.actorRef = actorRef;
    }

    public static Props props(ActorRef supervisorActorRef, String id, ActorRef actorRef) {
        return Props.create(WebSocketActor.class, () -> new WebSocketActor(supervisorActorRef, id, actorRef));
    }

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
                                System.out.println("ActorRef: " + actorRef);
                                supervisorRef.tell(message, actorRef);
                            });
                })
                .build();
    }

}
