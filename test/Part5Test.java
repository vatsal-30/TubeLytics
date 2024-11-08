
import model.Response;
import model.Video;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.test.WithApplication;
import services.YouTubeService;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class Part5Test extends WithApplication {

    private final static String TEXT = "In a quaint little village nestled between misty mountains and sprawling green fields, there was a peculiar shop that everyone called \"The Whispering Lantern.\" The shop appeared ordinary from the outside, with a simple wooden sign swaying gently in the breeze, but inside, it was anything but. Shelves brimmed with enchanted trinkets, ancient scrolls, and curious artifacts from distant lands. Visitors claimed that each item had a story to tell, and if you listened closely, you could hear faint whispers echoing through the lanterns hanging from the ceiling.";

    @Mock
    private YouTubeService youTubeService;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSearchVideoByKeyword() throws ExecutionException, InterruptedException {
        when(youTubeService.searchVideos(any(String.class))).thenReturn(CompletableFuture.supplyAsync(() -> {
            Response response = new Response();
            response.setQuery("Sample query about education and learning");
            response.setAverageFleschKincaidGradeLevel(7.36);
            response.setAverageFleschReadingScore(70.24);
            response.setVideos(new ArrayList<>());

            Video video = new Video("vid-001", "Understanding the Basics of Reading Levels", "An introduction to understanding reading levels and their impact on learning", "https://example.com/image1.jpg", "channel-001", "Education Today");
            video.setFleschKincaidGradeLevel(6.2);
            video.setFleschReadingScore(65.0);
            response.getVideos().add(video);

            video = new Video("vid-002", "Tips for Improving Reading Comprehension", "Tips for Improving Reading Comprehension", "https://example.com/image2.jpg", "channel-002", "Learning Insights");
            video.setFleschKincaidGradeLevel(8.1);
            video.setFleschReadingScore(70.3);
            response.getVideos().add(video);

            video = new Video("vid-003", "Advanced Strategies for Teaching Reading Skills", "Discover advanced strategies for teaching reading skills effectively.", "https://example.com/image3.jpg", "channel-003", "Teacher's Hub");
            video.setFleschKincaidGradeLevel(9.4);
            video.setFleschReadingScore(75.8);
            response.getVideos().add(video);

            video = new Video("vid-004", "The Importance of Reading for Young Learners", "Explores why reading is essential for early childhood education.", "https://example.com/image4.jpg", "channel-004", "Kids Academy");
            video.setFleschKincaidGradeLevel(5.8);
            video.setFleschReadingScore(68.2);
            response.getVideos().add(video);

            video = new Video("vid-005", "How to Foster a Love for Reading", "Ways to encourage a lifelong passion for reading in children.", "https://example.com/image5.jpg", "channel-005", "Book Lovers");
            video.setFleschKincaidGradeLevel(7.3);
            video.setFleschReadingScore(71.9);
            response.getVideos().add(video);

            return response;
        }));

        CompletionStage<Response> responseCompletionStage = youTubeService.searchVideos("Sample query about education and learning");
        Response response = responseCompletionStage.toCompletableFuture().get();

        Assert.assertNotNull(response);
        Assert.assertEquals("Sample query about education and learning", response.getQuery());
        Assert.assertEquals(5, response.getVideos().size());

    }
}
