package actor;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import play.libs.ws.WSClient;
import scala.concurrent.duration.Duration;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SupervisorActor extends AbstractActor {
    private final WSClient ws;
    private final String API_KEY;
    public Map<ActorRef, ActorRef> userActors = new ConcurrentHashMap<>();

    public SupervisorActor(WSClient wsClient, String apiKey) {
        this.ws = wsClient;
        this.API_KEY = apiKey;
    }

    public static Props props(WSClient wsClient, String apiKey) {
        return Props.create(SupervisorActor.class, () -> new SupervisorActor(wsClient, apiKey));
    }

    public static final class NotifyClient {
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(WebSocketActor.NewConnection.class, newConnection -> {
                    ActorRef userActor = getContext().actorOf(UserActor.props(getSender(), this.ws, this.API_KEY), "user-" + newConnection.getActorRef().path().name());
                    userActors.put(newConnection.getActorRef(), userActor);
                    getContext().watch(newConnection.getActorRef());
                })
                .match(WebSocketActor.Disconnected.class, disconnected -> {
                    ActorRef userActor = userActors.remove(disconnected.getActorRef());
                    if (userActor != null) {
                        getContext().stop(userActor);
                    }
                })
                .match(NotifyClient.class, notifyClient -> {
                    Set<ActorRef> actorRefs = userActors.keySet();
                    for (ActorRef actorRef : actorRefs) {
                        userActors.get(actorRef).tell(notifyClient, getSelf());
                    }
                })
                .matchAny(message -> {
                    if (message instanceof String) {
                        ActorRef userActor = userActors.get(getSender());
                        if (userActor != null) {
                            userActor.forward(message, getContext());
                        }
                    }
                })
                .match(Terminated.class, t -> {
                    userActors.entrySet().removeIf(entry -> entry.getKey().equals(t.getActor()));
                })
                .build();
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(
                10,
                Duration.create(40, TimeUnit.SECONDS),
                DeciderBuilder
                        .match(NullPointerException.class, e -> {
                            System.out.println("NullPointerException: " + e.getMessage());
                            return SupervisorStrategy.stop();
                        })
                        .match(Exception.class, e -> {
                            System.out.println("Exception: " + e.getMessage());
                            return SupervisorStrategy.restart();
                        })
                        .matchAny(o -> SupervisorStrategy.escalate())
                        .build()
        );
    }
}
