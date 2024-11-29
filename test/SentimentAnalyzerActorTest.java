import actor.SentimentAnalyzerActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import model.Response;
import model.Video;
import org.junit.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SentimentAnalyzerActorTest {

    private static ActorSystem actorSystem;

    private final String TEXT = "In a quaint little village nestled between misty mountains and sprawling green fields, there was a peculiar shop that everyone called \"The Whispering Lantern.\" The shop appeared ordinary from the outside, with a simple wooden sign swaying gently in the breeze, but inside, it was anything but. Shelves brimmed with enchanted trinkets, ancient scrolls, and curious artifacts from distant lands. Visitors claimed that each item had a story to tell, and if you listened closely, you could hear faint whispers echoing through the lanterns hanging from the ceiling.";
    private Response response = new Response();

    @BeforeClass
    public static void setup() {
        actorSystem = ActorSystem.create();
    }

    @AfterClass
    public static void tearDown() {
        TestKit.shutdownActorSystem(actorSystem);
        actorSystem = null;
    }

    @Before
    public void setUp() {
        response = new Response();
        response.setQuery("Sample query about education and learning");
        response.setAverageFleschKincaidGradeLevel(7.9);
        response.setAverageFleschReadingScore(70.37);
        response.setSentiment(":-|");
        response.setVideos(new ArrayList<>());

        Video video = new Video("vid-001", "Understanding the Basics of Reading Levels", "An introduction to understanding reading levels and their impact on learning", "https://example.com/image1.jpg", "channel-001", "Education Today");
        video.setDescription(TEXT);
        response.getVideos().add(video);
    }

    @Test
    public void testReadability() {
        final TestKit probe = new TestKit(actorSystem);
        final ActorRef actorRef = actorSystem.actorOf(SentimentAnalyzerActor.props(), "SentimentAnalyzerActor");

        actorRef.tell(response, probe.getRef());

        Response responseWithOverallSentiments = probe.expectMsgClass(Response.class);

        assertNotNull(responseWithOverallSentiments);
        assertNotNull(responseWithOverallSentiments.getSentiment());

        assertEquals(1, responseWithOverallSentiments.getVideos().size());

        assertEquals(":-|", responseWithOverallSentiments.getSentiment());
    }

    /**
     * Method for handling all happy sentiments
     *
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentimentForDescription_HappySentiment() {
        String description = "satisfied,";
        assertEquals(":-)", SentimentAnalyzerActor.analyzeSentimentForDescription(description));
    }

    /**
     * Method for handling all sad sentiments
     *
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentimentForDescription_SadSentiment() {
        String description = "hopelessness";
        assertEquals(":-(", SentimentAnalyzerActor.analyzeSentimentForDescription(description));
    }

    /**
     * Method for handling all neutral sentiments
     *
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentimentForDescription_NeutralSentiment() {
        String description = "It was a day with mixed feelings.";
        assertEquals(":-|", SentimentAnalyzerActor.analyzeSentimentForDescription(description));
    }

    /**
     * Method for handling when no input is given
     *
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentimentForDescription_NoWords() {
        String description = "";
        assertEquals(":-|", SentimentAnalyzerActor.analyzeSentimentForDescription(description));
    }

    /**
     * Method for handling data having less happy sentiments and more sad sentiments
     *
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentimentForDescription_LowHappyPercentage() {
        String description = "merrily tears cries vulnerably";
        assertEquals(":-(", SentimentAnalyzerActor.analyzeSentimentForDescription(description));
    }

    /**
     * Method for handling data having more happy sentiments and less sad sentiments
     *
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentimentForDescription_LowSadPercentage() {
        String description = "Cheer Rejuvenated Delight Grateful brightest funnest sad";
        assertEquals(":-)", SentimentAnalyzerActor.analyzeSentimentForDescription(description));
    }

    /**
     * Method for handling when no input is given
     *
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void emptyTest() {
        assertEquals(":-|", SentimentAnalyzerActor.analyzeSentimentForDescription(""));
    }

    /**
     * Method for handling when the entire list has happy sentiments
     *
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentiment_AllHappySentiments() {
        List<String> descriptions = Arrays.asList(
                "happy",
                "Joyful",
                "happy",
                "celebration",
                "luckily",
                "Blissful"
        );
        assertEquals(":-)", SentimentAnalyzerActor.analyzeSentiment(descriptions));
    }

    /**
     * Method for handling when the entire list has sad sentiments
     *
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentiment_AllSadSentiments() {
        List<String> descriptions = Arrays.asList(
                "sad",
                "unfortunate",
                "depressing"
        );
        assertEquals(":-(", SentimentAnalyzerActor.analyzeSentiment(descriptions));
    }

    /**
     * Method for handling when the entire list has neutral sentiments
     *
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentiment_AllNeutralSentiments() {
        List<String> descriptions = Arrays.asList(
                "It was a day",
                "Just an ordinary time",
                "No emotions here"
        );
        assertEquals(":-|", SentimentAnalyzerActor.analyzeSentiment(descriptions));
    }

    /**
     * Method for handling when the entire list has mixed sentiments
     *
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentiment_MixedSentiments() {
        List<String> descriptions = Arrays.asList(
                "positivity",
                "hopelessness"
        );
        assertEquals(":-|", SentimentAnalyzerActor.analyzeSentiment(descriptions));
    }

    /**
     * Method for handling when the entire list is empty
     *
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentiment_EmptyList() {
        List<String> descriptions = Collections.emptyList();
        assertEquals(":-|", SentimentAnalyzerActor.analyzeSentiment(descriptions));
    }

    /**
     * Method for handling when the majority of list has happy sentiments
     *
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentiment_MajorityHappySentiment() {
        List<String> descriptions = Arrays.asList(
                "pleasure", "blissfulness", "amazing", "gleeful", "euphoric", "miserableness", "red"
        );
        assertEquals(":-)", SentimentAnalyzerActor.analyzeSentiment(descriptions));
    }

    /**
     * Method for handling when the majority of list has sad sentiments
     *
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentiment_MajoritySadSentiment() {
        List<String> descriptions = Arrays.asList(
                "downed", "heartbreak", "sorrow", "anguishing", "distressed", "enthusiasm", "blue"
        );
        assertEquals(":-(", SentimentAnalyzerActor.analyzeSentiment(descriptions));
    }

    /**
     * Method for handling when the majority of list has neutral sentiments with minor sad sentiments
     *
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentiment_MajorityNeutralSadMinorSentiment() {
        List<String> descriptions = Arrays.asList(
                "loneliest", "Exuberantly", "Thrill", "mobile", "go", "red", "blue"
        );
        assertEquals(":-|", SentimentAnalyzerActor.analyzeSentiment(descriptions));
    }

    /**
     * Method for handling when the majority of list has neutral sentiments with minor happy sentiments
     *
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentiment_MajorityNeutralHappyMinorSentiment() {
        List<String> descriptions = Arrays.asList(
                "loneliest", "gloominess", "Hopeful", "mobile", "go", "red", "blue"
        );
        assertEquals(":-|", SentimentAnalyzerActor.analyzeSentiment(descriptions));
    }

    /**
     * Method for handling when the majority of list has neutral sentiments with average happy sentiments and a few sad sentiments
     *
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentiment_MajorityNeutralHappyAverageSadMinorSentiment() {
        List<String> descriptions = Arrays.asList(
                "lonelier", "positivity", "satisfaction", "fun", "enthusiastically", "radiant", "merrily", "mobile", "go", "red", "blue", "orange", "at", "the"
        );
        assertEquals(":-|", SentimentAnalyzerActor.analyzeSentiment(descriptions));
    }

}
