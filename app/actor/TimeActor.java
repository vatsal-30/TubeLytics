package actor;

import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorSelection;
import akka.actor.Props;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * TimeActor is responsible for sending periodic notifications to the SupervisorActor.
 * It uses a timer to send messages at regular intervals.
 * <br/>
 * Functionality:
 * - Sends a NotifyClient message to the SupervisorActor every 45 seconds.
 *
 * @author Utsav Patel
 */
public class TimeActor extends AbstractActorWithTimers {

    /**
     * Creates Props for the TimeActor.
     *
     * @return Props instance for TimeActor
     * @author Utsav Patel
     */
    public static Props getProps() {
        return Props.create(TimeActor.class);
    }

    /**
     * Internal message class used for timer ticks.
     *
     * @author Utsav Patel
     */
    private static final class Tick {
    }

    /**
     * Lifecycle hook that is called when the actor is started.
     * Starts a periodic timer to send Tick messages every 45 seconds.
     *
     * @author Utsav Patel
     */
    @Override
    public void preStart() {
        getTimers().startPeriodicTimer(
                "Timer",
                new Tick(),
                Duration.create(45, TimeUnit.SECONDS));
    }

    /**
     * Defines the behavior of the TimeActor.
     *
     * @return Receive instance defining message handling logic
     * @author Utsav Patel
     */
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
