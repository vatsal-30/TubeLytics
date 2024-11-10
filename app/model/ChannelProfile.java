package model;

import java.util.List;

/**
 * it contains all the necessary information about Channel to showcase all the information on the front-end.
 *
 * @author Amish Navadia
 */
public class ChannelProfile {
    private String name;
    private String imageUrl;
    private String description;
    private String subscriberCount;
    private String videoCount;
    private List<Video> videos; // List of videos

    // Constructor
    public ChannelProfile(String name, String imageUrl, String description,
                          String subscriberCount, String videoCount, List<Video> videos) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
        this.subscriberCount = subscriberCount;
        this.videoCount = videoCount;
        this.videos = videos;
    }

    // Getter and Setter methods

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubscriberCount() {
        return subscriberCount;
    }

    public void setSubscriberCount(String subscriberCount) {
        this.subscriberCount = subscriberCount;
    }

    public String getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(String videoCount) {
        this.videoCount = videoCount;
    }

    public List<Video> getVideos() {
        return videos;
    }

    public void setVideos(List<Video> videos) {
        this.videos = videos;
    }
}
