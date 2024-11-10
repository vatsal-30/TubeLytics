package services;

import model.Video;

import java.util.concurrent.CompletionStage;

public interface VideoService {
    CompletionStage<Video> getVideoById(String videoId);


}
