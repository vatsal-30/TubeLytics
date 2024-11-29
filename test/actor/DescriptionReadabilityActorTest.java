package actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import model.Response;
import model.Video;
import org.junit.*;

import java.util.ArrayList;

public class DescriptionReadabilityActorTest {

    private static ActorSystem actorSystem;
    private final String TEXT = "In a quaint little village nestled between misty mountains and sprawling green fields, there was a peculiar shop that everyone called \"The Whispering Lantern.\" The shop appeared ordinary from the outside, with a simple wooden sign swaying gently in the breeze, but inside, it was anything but. Shelves brimmed with enchanted trinkets, ancient scrolls, and curious artifacts from distant lands. Visitors claimed that each item had a story to tell, and if you listened closely, you could hear faint whispers echoing through the lanterns hanging from the ceiling.";
    private Response response = null;

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

    @BeforeClass
    public static void setup() {
        actorSystem = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(actorSystem);
        actorSystem = null;
    }

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
}
