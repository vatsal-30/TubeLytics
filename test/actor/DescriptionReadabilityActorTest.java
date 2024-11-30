package actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import model.Response;
import model.Video;
import org.junit.*;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Test class for DescriptionReadabilityActor.
 * <p>
 * This class contains unit tests for various methods in the DescriptionReadabilityActor
 * to validate their functionality, including readability calculations, sentence counting,
 * word splitting, and syllable counting.
 * </p>
 *
 * @author Utsav Patel
 */
public class DescriptionReadabilityActorTest {

    private static ActorSystem actorSystem;
    private final String TEXT = "In a quaint little village nestled between misty mountains and sprawling green fields, there was a peculiar shop that everyone called \"The Whispering Lantern.\" The shop appeared ordinary from the outside, with a simple wooden sign swaying gently in the breeze, but inside, it was anything but. Shelves brimmed with enchanted trinkets, ancient scrolls, and curious artifacts from distant lands. Visitors claimed that each item had a story to tell, and if you listened closely, you could hear faint whispers echoing through the lanterns hanging from the ceiling.";
    private Response response = null;

    /**
     * Sets up the test environment by initializing the Response object
     * with sample data for testing.
     */
    @Before
    public void setUp() {
        response = new Response();
        response.setQuery("Sample query about education and learning");
        response.setAverageFleschKincaidGradeLevel(7.9);
        response.setAverageFleschReadingScore(70.37);
        response.setSentiment(":-)");
        response.setVideos(new ArrayList<>());

        Video video = new Video("vid-001", "Understanding the Basics of Reading Levels", "An introduction to understanding reading levels and their impact on learning", "https://example.com/image1.jpg", "channel-001", "Education Today");
        video.setDescription(TEXT);
        response.getVideos().add(video);
    }

    /**
     * Sets up the ActorSystem for testing.
     */
    @BeforeClass
    public static void setup() {
        actorSystem = ActorSystem.create();
    }

    /**
     * Tears down the ActorSystem after all tests are executed.
     */
    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(actorSystem);
        actorSystem = null;
    }

    /**
     * Tests the readability calculation logic in the DescriptionReadabilityActor.
     *
     * @author Utsav Patel
     */
    @Test
    public void testReadability() {
        final TestKit probe = new TestKit(actorSystem);
        final ActorRef actorRef = actorSystem.actorOf(DescriptionReadabilityActor.props(), "DescriptionReadabilityActor");

        actorRef.tell(response, probe.getRef());

        Response responseWithReadabilityScore = probe.expectMsgClass(Response.class);

        Assert.assertNotNull(responseWithReadabilityScore);
        Assert.assertNotNull(responseWithReadabilityScore.getAverageFleschKincaidGradeLevel());
        Assert.assertNotNull(responseWithReadabilityScore.getAverageFleschReadingScore());

        Assert.assertEquals(1, responseWithReadabilityScore.getVideos().size());

        // Between 10 and 12
        Assert.assertTrue(10.00 < responseWithReadabilityScore.getAverageFleschKincaidGradeLevel());
        Assert.assertTrue(12.00 > responseWithReadabilityScore.getAverageFleschKincaidGradeLevel());

        // Between 50 and 60
        Assert.assertTrue(50 < responseWithReadabilityScore.getAverageFleschReadingScore());
        Assert.assertTrue(60 > responseWithReadabilityScore.getAverageFleschReadingScore());

        Video video = responseWithReadabilityScore.getVideos().get(0);

        Assert.assertTrue(10.00 < video.getFleschKincaidGradeLevel());
        Assert.assertTrue(12.00 > video.getFleschKincaidGradeLevel());

        Assert.assertTrue(50 < video.getFleschReadingScore());
        Assert.assertTrue(60 > video.getFleschReadingScore());
    }

    /**
     * Tests the sentence counting functionality of the DescriptionReadabilityActor.
     *
     * @author Utsav Patel
     */
    @Test
    public void countSentenceTest() {
        int countSentences = DescriptionReadabilityActor.countSentences(TEXT);
        Assert.assertEquals(4, countSentences);
    }

    /**
     * Tests the word splitting functionality of the DescriptionReadabilityActor.
     *
     * @author Utsav Patel
     */
    @Test
    public void splitIntoWordsTest() {
        String[] words = DescriptionReadabilityActor.splitIntoWords(TEXT);
        Assert.assertNotNull(words);
        Assert.assertEquals(88, words.length);
        Assert.assertEquals("In", words[0]);
        Assert.assertEquals("a", words[1]);
    }

    /**
     * Tests the consonant detection functionality of the DescriptionReadabilityActor.
     *
     * @author Utsav Patel
     */
    @Test
    public void consonantTest() {
        Assert.assertFalse(DescriptionReadabilityActor.isConsonant('a'));
        Assert.assertTrue(DescriptionReadabilityActor.isConsonant('b'));
    }

    /**
     * Tests the syllable counting functionality of the DescriptionReadabilityActor.
     *
     * @author Utsav Patel
     */
    @Test
    public void countSyllablesTest() {
        assertEquals(1, DescriptionReadabilityActor.countSyllables("a"));
        assertEquals(1, DescriptionReadabilityActor.countSyllables("cake"));
        assertEquals(3, DescriptionReadabilityActor.countSyllables("elephant"));
        assertEquals(1, DescriptionReadabilityActor.countSyllables("table"));
        assertEquals(3, DescriptionReadabilityActor.countSyllables("tablee"));
        assertEquals(2, DescriptionReadabilityActor.countSyllables("taalee"));
        assertEquals(1, DescriptionReadabilityActor.countSyllables("whale"));
        assertEquals(2, DescriptionReadabilityActor.countSyllables("waleed"));
        assertEquals(1, DescriptionReadabilityActor.countSyllables("played"));
        assertEquals(1, DescriptionReadabilityActor.countSyllables("watches"));
        assertEquals(2, DescriptionReadabilityActor.countSyllables("agrees"));
        assertEquals(1, DescriptionReadabilityActor.countSyllables("rhythm"));
        assertEquals(2, DescriptionReadabilityActor.countSyllables("HeLLo "));
        assertEquals(3, DescriptionReadabilityActor.countSyllables("beautiful"));
        assertEquals(1, DescriptionReadabilityActor.countSyllables("red"));
        assertEquals(1, DescriptionReadabilityActor.countSyllables("yes"));
        assertEquals(1, DescriptionReadabilityActor.countSyllables("aeiou"));
    }

    /**
     * Tests the readability score calculation functionality of the DescriptionReadabilityActor.
     *
     * @author Utsav Patel
     */
    @Test
    public void calculateReadabilityScoresTest() {
        double[] calculateReadabilityScores = DescriptionReadabilityActor.calculateReadabilityScores(TEXT);
        // Between 10 and 12
        Assert.assertTrue(10 < calculateReadabilityScores[0]);
        Assert.assertTrue(12 > calculateReadabilityScores[0]);

        // Between 50 and 60
        Assert.assertTrue(50 < calculateReadabilityScores[1]);
        Assert.assertTrue(60 > calculateReadabilityScores[1]);

        calculateReadabilityScores = DescriptionReadabilityActor.calculateReadabilityScores("");
        Assert.assertEquals(Double.valueOf(0.0), Double.valueOf(calculateReadabilityScores[0]));
        Assert.assertEquals(Double.valueOf(0.0), Double.valueOf(calculateReadabilityScores[1]));
    }
}
