package services;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import model.Video;

public interface YouTubeService {
        Source<Video, NotUsed> searchVideos(String keyword);
}
