package services;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class SentimentAnalyzer {

    // Expanded sets of happy and sad words (add more words as needed)
    private static final Set<String> happyWords = Set.of(
            "happy", "joy", "excited", ":-)", "😊", "🙂", "love", "good", "great", "amazing", "fun", "awesome", "cheerful", "elated", "content"
    );

    private static final Set<String> sadWords = Set.of(
            "sad", "unhappy", "depressed", ":-(", "😢", "☹️", "bad", "awful", "miserable", "down", "heartbroken", "angry", "hopeless"
    );

    /**
     * Analyzes the sentiment of a single description.
     *
     * @param description The description to analyze.
     * @return A string representing the sentiment ("Happy", "Sad", or "Neutral").
     */
    public String analyzeSentimentForDescription(String description) {
        // Count the number of happy and sad words in the description
        long happyCount = Arrays.stream(description.toLowerCase().split("\\s+"))
                .filter(happyWords::contains)
                .count();

        long sadCount = Arrays.stream(description.toLowerCase().split("\\s+"))
                .filter(sadWords::contains)
                .count();

        // Calculate the total word count for normalization
        long totalWords = description.split("\\s+").length;

        // Prevent division by zero if no words are present
        if (totalWords == 0) {
            return ":-|"; // Neutral if no words
        }

        // Calculate the percentage of happy and sad words in the description
        double happyPercentage = (double) happyCount / totalWords;
        double sadPercentage = (double) sadCount / totalWords;

        // Determine sentiment based on the counts
        if (happyPercentage > 0.7) {
            return ":-)"; // More than 70% happy words means positive sentiment
        } else if (sadPercentage > 0.7) {
            return ":-("; // More than 70% sad words means negative sentiment
        } else {
            return ":-|"; // Otherwise, the sentiment is neutral
        }
    }

    /**
     * Analyzes the sentiment of a list of video descriptions.
     *
     * @param descriptions List of descriptions to analyze.
     * @return A string representing the overall sentiment ("Happy", "Sad", or "Neutral").
     */
    public String analyzeSentiment(List<String> descriptions) {
        // Count how many descriptions are happy, sad, or neutral
        long happyCount = descriptions.stream()
                .map(this::analyzeSentimentForDescription)
                .filter(s -> s.equals(":-)"))
                .count();

        long sadCount = descriptions.stream()
                .map(this::analyzeSentimentForDescription)
                .filter(s -> s.equals(":-("))
                .count();

        long neutralCount = descriptions.stream()
                .map(this::analyzeSentimentForDescription)
                .filter(s -> s.equals(":-|"))
                .count();

        long totalCount = happyCount + sadCount + neutralCount;

        // Calculate the percentage of happy, sad, and neutral sentiments
        double happyPercentage = (double) happyCount / totalCount;
        double sadPercentage = (double) sadCount / totalCount;

        // Determine the overall sentiment based on the percentages
        if (happyPercentage > 0.7) {
            return ":-)"; // More than 70% happy descriptions means overall happy sentiment
        } else if (sadPercentage > 0.7) {
            return ":-("; // More than 70% sad descriptions means overall sad sentiment
        } else {
            return ":-|"; // Otherwise, overall neutral sentiment
        }
    }
}
