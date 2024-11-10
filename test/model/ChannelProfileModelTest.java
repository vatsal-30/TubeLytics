package model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class ChannelProfileModelTest {
    private ChannelProfile channelProfile;

    @Before
    public void setUp() {
        channelProfile = new ChannelProfile("channel-001", "https://example.com/image1.jpg", "channel-001's description", "1000000", "9999", new ArrayList<>());
        Video video = new Video("vid-001", "Understanding the Basics of Reading Levels", "An introduction to understanding reading levels and their impact on learning", "https://example.com/image1.jpg", "channel-001", "channel-001's description");
        video.setFleschKincaidGradeLevel(6.2);
        video.setFleschReadingScore(65.0);
        channelProfile.getVideos().add(video);

        video = new Video("vid-002", "Tips for Improving Reading Comprehension", "Tips for Improving Reading Comprehension", "https://example.com/image2.jpg", "channel-001", "channel-001's description");
        video.setFleschKincaidGradeLevel(8.1);
        video.setFleschReadingScore(70.3);
        channelProfile.getVideos().add(video);

        video = new Video("vid-003", "Advanced Strategies for Teaching Reading Skills", "Discover advanced strategies for teaching reading skills effectively.", "https://example.com/image3.jpg", "channel-001", "channel-001's description");
        video.setFleschKincaidGradeLevel(9.4);
        video.setFleschReadingScore(75.8);
        channelProfile.getVideos().add(video);

        video = new Video("vid-004", "The Importance of Reading for Young Learners", "Explores why reading is essential for early childhood education.", "https://example.com/image4.jpg", "channel-001", "channel-001's description");
        video.setFleschKincaidGradeLevel(5.8);
        video.setFleschReadingScore(68.2);
        channelProfile.getVideos().add(video);

        video = new Video("vid-005", "How to Foster a Love for Reading", "Ways to encourage a lifelong passion for reading in children.", "https://example.com/image5.jpg", "channel-001", "channel-001's description");
        video.setFleschKincaidGradeLevel(7.3);
        video.setFleschReadingScore(71.9);
        channelProfile.getVideos().add(video);
    }

    @Test
    public void channelProfileTest() {
        Assert.assertEquals("channel-001", channelProfile.getName());
        Assert.assertEquals("channel-001's description", channelProfile.getDescription());
        Assert.assertEquals("https://example.com/image1.jpg", channelProfile.getImageUrl());
        Assert.assertEquals("1000000", channelProfile.getSubscriberCount());
        Assert.assertEquals("9999", channelProfile.getVideoCount());
        Assert.assertEquals(5, channelProfile.getVideos().size());
    }

    @Test
    public void channelProfileModelGetterTest() {
        channelProfile.setName("channel-002");
        Assert.assertEquals("channel-002", channelProfile.getName());
        channelProfile.setDescription("channel-002's description");
        Assert.assertEquals("channel-002's description", channelProfile.getDescription());
        channelProfile.setImageUrl("channel-002.jpg");
        Assert.assertEquals("channel-002.jpg", channelProfile.getImageUrl());
        channelProfile.setSubscriberCount("200000");
        Assert.assertEquals("200000", channelProfile.getSubscriberCount());
        channelProfile.setVideoCount("100000");
        Assert.assertEquals("100000", channelProfile.getVideoCount());
        channelProfile.setVideos(new ArrayList<>());
        Assert.assertEquals(0, channelProfile.getVideos().size());
    }
}
