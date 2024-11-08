package model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class VideoModelTest {
    private Video video;

    @Before
    public void setUp() {
        this.video = new Video("vid-001", "Understanding the Basics of Reading Levels", "An introduction to understanding reading levels and their impact on learning", "https://example.com/image1.jpg", "channel-001", "Education Today");
        this.video.setFleschKincaidGradeLevel(6.2);
        this.video.setFleschReadingScore(65.0);
    }

    @Test
    public void testVideo() {
        Assert.assertEquals("vid-001", video.getVideoId());
        Assert.assertEquals("Understanding the Basics of Reading Levels", video.getTitle());
        Assert.assertEquals("An introduction to understanding reading levels and their impact on learning", video.getDescription());
        Assert.assertEquals("https://example.com/image1.jpg", video.getImageUrl());
        Assert.assertEquals("channel-001", video.getChannelId());
        Assert.assertEquals("Education Today", video.getChannelTitle());
        Assert.assertEquals(Double.valueOf(6.2), video.getFleschKincaidGradeLevel());
        Assert.assertEquals(Double.valueOf(65.0), video.getFleschReadingScore());
    }
}
