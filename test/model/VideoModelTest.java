package model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Utsav Patel
 */
public class VideoModelTest {
    private Video video;

    /**
     * This method sets up a dummy Video object to test its constructors, getters, and setters.
     *
     * @author Utsav Patel
     */
    @Before
    public void setUp() {
        this.video = new Video("vid-001", "Understanding the Basics of Reading Levels", "An introduction to understanding reading levels and their impact on learning", "https://example.com/image1.jpg", "channel-001", "Education Today");
        this.video.setFleschKincaidGradeLevel(6.2);
        this.video.setFleschReadingScore(65.0);
    }

    /**
     * This method tests Video object.
     *
     * @author Utsav Patel
     */
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

    /**
     * This method tests Video object setters.
     *
     * @author Utsav Patel
     */
    @Test
    public void testVideoSetters() {
        Video newVideo = new Video();
        newVideo.setVideoId(video.getVideoId());
        Assert.assertEquals("vid-001", newVideo.getVideoId());
        newVideo.setTitle(video.getTitle());
        Assert.assertEquals("Understanding the Basics of Reading Levels", newVideo.getTitle());
        newVideo.setDescription(video.getDescription());
        Assert.assertEquals("An introduction to understanding reading levels and their impact on learning", newVideo.getDescription());
        newVideo.setImageUrl(video.getImageUrl());
        Assert.assertEquals("https://example.com/image1.jpg", newVideo.getImageUrl());
        newVideo.setChannelId(video.getChannelId());
        Assert.assertEquals("channel-001", newVideo.getChannelId());
        newVideo.setChannelTitle(video.getChannelTitle());
        Assert.assertEquals("Education Today", newVideo.getChannelTitle());

        Video otherVideo = new Video(video.getVideoId(), video.getTitle(), video.getDescription(), video.getImageUrl(), video.getChannelId(), video.getChannelTitle(), "tag1, tag2");
        newVideo.setTags(otherVideo.getTags());
        Assert.assertEquals("tag1, tag2", newVideo.getTags());
    }
}
