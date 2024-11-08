package services;

import model.ChannelProfile;
import model.Response;

import java.util.concurrent.CompletionStage;

public interface YouTubeService {
        CompletionStage<Response> searchVideos(String keyword);

        CompletionStage<ChannelProfile> getChannelProfile(String channelId);


}
