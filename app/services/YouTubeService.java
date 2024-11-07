package services;

import model.Response;

import java.util.concurrent.CompletionStage;

public interface YouTubeService {
        CompletionStage<Response> searchVideos(String keyword);
//        Source<Video, NotUsed> searchVideos(String keyword);
}
