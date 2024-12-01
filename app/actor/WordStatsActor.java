package actor;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import services.YouTubeService;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class WordStatsActor extends AbstractActor {

    private final YouTubeService youTubeService;

    /**
     * Constructor to initialize the WordStatsActor with a YouTubeService instance.
     *
     * @param youTubeService the YouTubeService instance to interact with
     * @author Karan Tanakhia
     */
    public WordStatsActor(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    /**
     * Factory method to create a new instance of the WordStatsActor.
     *
     * @author Karan Tanakhia
     * @param youTubeService the YouTubeService instance to interact with
     * @return Props the Props object to create the actor
     */
    public static Props props(YouTubeService youTubeService) {
        return Props.create(WordStatsActor.class, () -> new WordStatsActor(youTubeService));
    }


    /**
     * The createReceive method defines how the actor responds to incoming messages.
     * In this case, it listens for a keyword (String), and based on the keyword, it either fetches word stats
     * from the YouTubeService or sends an appropriate response back to the sender.
     *
     * @author Karan Tanakhia
     * @return Receive the behavior of the actor
     */
    @Override
    public Receive createReceive() {

        return receiveBuilder()
                .match(String.class, keyword -> {
                    if (keyword == null || keyword.trim().isEmpty()) {
                        ActorRef actorRef = getSender();
                        actorRef.tell("No word stats found for: " + (keyword != null ? keyword : ""), getSelf());
                        return;
                    }
                    ActorRef actorRef = getSender();
                    fetchWordStats(keyword)
                            .thenAccept(stats -> {
                                if (stats == null ||stats.isEmpty()) {

                                    actorRef.tell("No word stats found for: " +  keyword,getSelf());
                                } else {
                                    actorRef.tell(stats, getSelf());
                                }
                            }).exceptionally(ex -> {
                                actorRef.tell("Error occurred while fetching word stats: " + ex.getMessage(), getSelf());
                                return null;
                            });;
                })
                .build();
    }

    /**
     * Fetches the word statistics for a given keyword by invoking the YouTubeService.
     *
     * @author Karan Tanakhia
     * @param keyword The keyword to search for.
     * @return CompletionStage of List<String> containing word statistics.
     */
    private CompletionStage<List<String>> fetchWordStats(String keyword) {

        return youTubeService.wordStatesVideos(keyword);

    }


}

