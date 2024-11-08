package services;

import model.ChannelProfile;
import model.Response;
import model.Video;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface YouTubeService {
        CompletionStage<Response> searchVideos(String keyword);
//        Source<Video, NotUsed> searchVideos(String keyword);
        CompletionStage<ChannelProfile> getChannelProfile(String channelId);

        CompletionStage<List<Video>> getChannelVideos(String channelId, int i);
}
