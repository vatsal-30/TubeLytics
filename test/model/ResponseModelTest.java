package model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class ResponseModelTest {
    private Response response;

    @Before
    public void setUp() {
        response = new Response();
        response.setQuery("Sample query about education and learning");
        response.setAverageFleschKincaidGradeLevel(7.9);
        response.setAverageFleschReadingScore(70.37);
        response.setSentiment(":-)");
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
    }

    @Test
    public void responseTest(){
        Assert.assertEquals("Sample query about education and learning", response.getQuery());
        Assert.assertEquals(Double.valueOf(7.9), response.getAverageFleschKincaidGradeLevel());
        Assert.assertEquals(Double.valueOf(70.37), response.getAverageFleschReadingScore());
        Assert.assertNotNull(response.getVideos());
        Assert.assertEquals(":-)", response.getSentiment());
        Assert.assertEquals(3, response.getVideos().size());
    }


}
