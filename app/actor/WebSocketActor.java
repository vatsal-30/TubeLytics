package actor;

import akka.actor.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Response;
import scala.concurrent.Future;
import scala.jdk.javaapi.FutureConverters;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;

import static akka.pattern.Patterns.ask;

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
//                .match(Response.class, response -> {
//                    System.out.println("Response: " + response);
//                    System.out.println("ActorRef: " + actorRef);
//                    if (actorRef != null) {
//                        actorRef.tell(serializeResponse(response), getSelf());
//                    }
//                })
                .match(String.class, message -> {
                    String actorPath = "akka://application/user/websocket-" + id;
                    ActorSelection actorSelection = context().actorSelection(actorPath);

                    actorSelection.resolveOne(Duration.ofSeconds(2)).toCompletableFuture()
                            .thenAccept(actorRef -> {
                                System.out.println("ActorRef: " + actorRef);
                                supervisorRef.tell(message, actorRef);
                            });

//                    FutureConverters.asJava(
//                                    ask(supervisorRef, message, 2000))
//                            .thenApply(response -> {
//                                if (response instanceof Response) {
//                                    getSender().tell(serializeResponse((Response) response), getSelf());
//                                }
//                                return null;
//                            });
                })
                .build();
    }

}
