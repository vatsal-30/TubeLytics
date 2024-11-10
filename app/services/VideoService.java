package services;

import model.Video;

import java.util.concurrent.CompletionStage;

/**
 * @author Yash Ajmeri
 */
public interface VideoService {
    CompletionStage<Video> getVideoById(String videoId);


}
