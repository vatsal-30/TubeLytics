package actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import model.Response;

public class DescriptionReadabilityActor extends AbstractActor {

    public static Props props() {
        return Props.create(DescriptionReadabilityActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Response.class, response -> {
            // TODO: process the response - calculate Description Readability Score
            getSender().tell(response, getSelf());
        }).build();
    }
}
