package services;

import model.ChannelProfile;
import model.Response;
import model.Video;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * @author Utsav Patel
 */
public interface YouTubeService {
        CompletionStage<Response> searchVideos(String keyword);
        //        Source<Video, NotUsed> searchVideos(String keyword);

        /**
         * @author Amish Navadia
         */
        CompletionStage<ChannelProfile> getChannelProfile(String channelId);

        /**
         * @author Amish Navadia
         */
        CompletionStage<List<Video>> getChannelVideos(String channelId, int i);

        /**
         * @author Karan Tanakhia
         */
        CompletionStage<List<String>> wordStatesVideos(String keyword);
}
