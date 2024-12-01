package Actor;

import actor.WordStatsActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import services.YouTubeService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;

public class WordStatsActorTest {


    private static ActorSystem actorSystem;

    @Mock
    private YouTubeService youTubeService;

    /**
     * This method sets up the ActorSystem before running the tests.
     * It is executed once before any tests in the class.
     */
    @BeforeClass
    public static void setupClass() {
        actorSystem = ActorSystem.create("TestActorSystem");
    }


    /**
     * This method sets up the mock objects before each test.
     * It initializes the mock objects using Mockito.
     */
    @Before
    public void setUp() {

        MockitoAnnotations.initMocks(this);
    }

    /**
     * This test validates the behavior of the WordStatsActor when a valid keyword is provided.
     * It mocks the YouTubeService to return a predefined list of word statistics and checks if the
     * actor responds with the correct data.
     */
    @Test
    public void testWordStatsActor_validKeyword() {
        TestKit probe = new TestKit(actorSystem);
        String keyword = "example";
        List<String> mockWordStats = List.of("word1: 5", "word2: 3", "word3: 1");

        when(youTubeService.wordStatesVideos(keyword))
                .thenReturn(CompletableFuture.completedFuture(mockWordStats));

        ActorRef wordStatsActor = actorSystem.actorOf(WordStatsActor.props(youTubeService));

        wordStatsActor.tell(keyword, probe.getRef());

        probe.expectMsg(mockWordStats);
    }

    /**
     * This test validates the behavior of the WordStatsActor when no results are found for a keyword.
     * It mocks the YouTubeService to return an empty list and verifies that the actor responds
     * with the appropriate message indicating no results were found.
     */
    @Test
    public void testWordStatsActor_noResults() {
        TestKit probe = new TestKit(actorSystem);
        String keyword = "unknown";

        when(youTubeService.wordStatesVideos(keyword))
                .thenReturn(CompletableFuture.completedFuture(List.of()));

        ActorRef wordStatsActor = actorSystem.actorOf(WordStatsActor.props(youTubeService));

        wordStatsActor.tell(keyword, probe.getRef());

        probe.expectMsg("No word stats found for: " + keyword);
    }

    /**
     * This test validates the behavior of the WordStatsActor when an error occurs while fetching word stats.
     * It mocks the YouTubeService to throw an exception and verifies that the actor handles the exception
     * and responds with the correct error message.
     */
    @Test
    public void testWordStatsActor_errorHandling() {
        TestKit probe = new TestKit(actorSystem);
        String keyword = "errorExample";

        when(youTubeService.wordStatesVideos(keyword))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Mocked Exception")));

        ActorRef wordStatsActor = actorSystem.actorOf(WordStatsActor.props(youTubeService));

        wordStatsActor.tell(keyword, probe.getRef());

        probe.expectMsg("Error occurred while fetching word stats: java.lang.RuntimeException: Mocked Exception");
    }

    /**
     * This test validates the behavior of the WordStatsActor when a null or empty keyword is provided.
     * It checks that the actor responds with a message indicating no word stats were found for the empty keyword.
     */
    @Test
    public void testWordStatsActor_nullOraEmptyKeyword() {
        TestKit probe = new TestKit(actorSystem);

        ActorRef wordStatsActor = actorSystem.actorOf(WordStatsActor.props(youTubeService));

        wordStatsActor.tell("", probe.getRef());
        probe.expectMsg("No word stats found for: ");

    }

    /**
     * This method shuts down the ActorSystem after all tests have completed.
     * It is executed once after all tests in the class.
     */
    @AfterClass
    public static void tearDownClass() {
        TestKit.shutdownActorSystem(actorSystem);
        actorSystem = null;
    }
}
