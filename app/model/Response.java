package model;

import java.util.List;

/**
 * It contains all the necessary information about Response to showcase all the information on the front-end.
 *
 * @author Utsav Patel
 */
public class Response {
    private String query;
    private Double averageFleschKincaidGradeLevel;
    private Double averageFleschReadingScore;
    private List<Video> videos;
    private String sentiment;
    private boolean isFromKeyword;

    public Response() {
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Double getAverageFleschKincaidGradeLevel() {
        return averageFleschKincaidGradeLevel;
    }

    public void setAverageFleschKincaidGradeLevel(Double averageFleschKincaidGradeLevel) {
        this.averageFleschKincaidGradeLevel = averageFleschKincaidGradeLevel;
    }

    public Double getAverageFleschReadingScore() {
        return averageFleschReadingScore;
    }

    public void setAverageFleschReadingScore(Double averageFleschReadingScore) {
        this.averageFleschReadingScore = averageFleschReadingScore;
    }

    public List<Video> getVideos() {
        return videos;
    }

    public void setVideos(List<Video> videos) {
        this.videos = videos;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public boolean isFromKeyword() {
        return isFromKeyword;
    }

    public void setFromKeyword(boolean fromKeyword) {
        isFromKeyword = fromKeyword;
    }
}
