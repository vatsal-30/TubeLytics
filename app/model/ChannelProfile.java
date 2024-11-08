package model;

public class ChannelProfile {
    private String name;
    private String imageUrl;
    private String description;
    private String subscriberCount;
    private String videoCount;

    // Constructor
    public ChannelProfile(String name, String imageUrl, String description, String subscriberCount, String videoCount) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
        this.subscriberCount = subscriberCount;
        this.videoCount = videoCount;
    }

    public ChannelProfile(String text, String text1, String text2) {
    }

    // Getter and Setter for name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter and Setter for imageUrl
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Getter and Setter for description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Getter and Setter for subscriberCount
    public String getSubscriberCount() {
        return subscriberCount;
    }

    public void setSubscriberCount(String subscriberCount) {
        this.subscriberCount = subscriberCount;
    }

    // Getter and Setter for videoCount
    public String getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(String videoCount) {
        this.videoCount = videoCount;
    }
}
