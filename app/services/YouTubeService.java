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
         * This method will fetch the videos from the YouTube API based on the provided keyword and then return the list of word and it's frequency from the all videos description .
         *
         * @author Karan Tanakhia
         * @param keyword the search keyword for which to retrieve video descriptions
         * @return CompletionStage<List<String>> a list of word statistics calculated from the video descriptions
         */
        CompletionStage<List<String>> wordStatesVideos(String keyword);
}
