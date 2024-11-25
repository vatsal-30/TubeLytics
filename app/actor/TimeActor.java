package actor;

import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorSelection;
import akka.actor.Props;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class TimeActor extends AbstractActorWithTimers {

    public static Props getProps() {
        return Props.create(TimeActor.class);
    }

    private static final class Tick {
    }

    @Override
    public void preStart() {
        getTimers().startPeriodicTimer(
                "Timer",
                new Tick(),
                Duration.create(45, TimeUnit.SECONDS));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Tick.class, message -> {
                    String actorPath = "akka://application/user/supervisor";
                    ActorSelection actorSelection = context().actorSelection(actorPath);

                    actorSelection.resolveOne(java.time.Duration.ofSeconds(2)).toCompletableFuture()
                            .thenAccept(actorRef -> {
                                actorRef.tell(new SupervisorActor.NotifyClient(), getSelf());
                            });
                })
                .build();
    }
}
