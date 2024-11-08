package model;

import java.util.Arrays;
import java.util.List;

public class Video {
    private String videoId;
    private String title;
    private String description;
    private String imageUrl;
    private String channelId;
    private String channelTitle;
    private Double fleschKincaidGradeLevel;
    private Double FleschReadingScore;

    private String tags;

    public Video() {
    }

    public Video(String videoId, String title, String description, String imageUrl, String channelId, String channelTitle) {
        this.videoId = videoId;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.channelId = channelId;
        this.channelTitle = channelTitle;
    }

    public Video(String videoId, String title, String description, String imageUrl, String channelId, String channelTitle, String tags) {
        this.videoId = videoId;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.channelId = channelId;
        this.channelTitle = channelTitle;
        this.tags=tags;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTags() {
//        System.out.println(Arrays.toString(tags));
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelTitle() {
        return channelTitle;
    }

    public void setChannelTitle(String channelTitle) {
        this.channelTitle = channelTitle;
    }

    public Double getFleschKincaidGradeLevel() {
        return fleschKincaidGradeLevel;
    }

    public void setFleschKincaidGradeLevel(Double fleschKincaidGradeLevel) {
        this.fleschKincaidGradeLevel = fleschKincaidGradeLevel;
    }

    public Double getFleschReadingScore() {
        return FleschReadingScore;
    }

    public void setFleschReadingScore(Double fleschReadingScore) {
        FleschReadingScore = fleschReadingScore;
    }
}
