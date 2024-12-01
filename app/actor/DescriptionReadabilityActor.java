package actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import model.Response;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Actor to calculate readability scores for video descriptions.
 * Processes messages of type {@link Response}, calculates readability metrics,
 * and updates the response object with the calculated scores.
 *
 * @author Utsav Patel
 */
public class DescriptionReadabilityActor extends AbstractActor {

    /**
     * Creates a Props instance for {@link DescriptionReadabilityActor}.
     *
     * @return Props instance for this actor
     * @author Utsav Patel
     */
    public static Props props() {
        return Props.create(DescriptionReadabilityActor.class);
    }

    /**
     * Defines the behavior of the actor for handling messages.
     *
     * @return Receive instance defining message handling logic
     * @author Utsav Patel
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Response.class, response -> {
            calculateDescriptionReadability(response);
            getSender().tell(response, getSelf());
        }).build();
    }

    /**
     * Calculates the readability scores for video descriptions in the given response.
     * Updates the response object with average readability scores.
     *
     * @param response {@link Response} containing video descriptions
     * @author Utsav Patel
     */
    public void calculateDescriptionReadability(Response response) {
        AtomicReference<Double> fkg = new AtomicReference<>(0.0);
        AtomicReference<Double> frs = new AtomicReference<>(0.0);
        response.getVideos().stream()
                .forEach(video -> {
                    double[] readabilityScores = calculateReadabilityScores(video.getDescription());
                    fkg.updateAndGet(v -> (v + readabilityScores[0]));
                    video.setFleschKincaidGradeLevel(readabilityScores[0]);
                    frs.updateAndGet(v -> (v + readabilityScores[1]));
                    video.setFleschReadingScore(readabilityScores[1]);
                });
        int size = response.getVideos().size();
        response.setAverageFleschKincaidGradeLevel(fkg.get() / size);
        response.setAverageFleschReadingScore(frs.get() / size);
    }

    /**
     * Calculates readability scores (Flesch-Kincaid Grade Level and Flesch Reading Score) for a given text.
     *
     * @param description the text to analyze
     * @return an array of doubles where index 0 is the Flesch-Kincaid Grade Level
     * and index 1 is the Flesch Reading Score
     * @author Utsav Patel
     */
    public static double[] calculateReadabilityScores(String description) {
        if (description.isEmpty()) {
            return new double[]{0, 0};
        }

        String[] words = splitIntoWords(description);
        int totalWords = words.length;
        int totalSentences = countSentences(description);

        int totalSyllables = 0;

        for (String word : words) {
            totalSyllables += countSyllables(word);
        }

        double wordsPerSentence = (double) totalWords / totalSentences;
        double syllablesPerWord = (double) totalSyllables / totalWords;
        double fkg = 0.39 * wordsPerSentence + 11.8 * syllablesPerWord - 15.59;
        double frs = 206.835 - 1.015 * wordsPerSentence - 84.6 * syllablesPerWord;
        return new double[]{fkg, frs};
    }

    /**
     * Counts the number of syllables in a word.
     *
     * @param word the word to analyze
     * @return the number of syllables in the word
     * @author Utsav Patel
     */
    public static int countSyllables(String word) {
        word = word.toLowerCase().trim();
        if (word.length() == 1) {
            return 1;
        }

        if (word.endsWith("e")) {
            word = word.substring(0, word.length() - 1);
        }

        String[] vowelGroups = word.split("[^aeiouy]+");
        int syllableCount = 0;
        for (String group : vowelGroups) {
            if (!group.isEmpty()) {
                syllableCount++;
            }
        }

        if (word.length() > 2) {
            if (word.endsWith("le")) {
                if (isConsonant(word.charAt(word.length() - 3))) {
                    syllableCount++;
                }
            }
        }

        if (word.endsWith("ed") && syllableCount > 1) {
            if (isConsonant(word.charAt(word.length() - 3))) {
                syllableCount--;
            }
        }

        if (word.endsWith("es") && syllableCount > 1) {
            if (isConsonant(word.charAt(word.length() - 3))) {
                syllableCount--;
            }
        }

        return Math.max(syllableCount, 1);
    }

    /**
     * Determines if a character is a consonant.
     *
     * @param c the character to check
     * @return true if the character is a consonant, false otherwise
     * @author Utsav Patel
     */
    public static boolean isConsonant(char c) {
        return "bcdfghjklmnpqrstvwxyz".indexOf(c) >= 0;
    }

    /**
     * Splits a text into an array of words.
     *
     * @param text the text to split
     * @return an array of words
     * @author Utsav Patel
     */
    public static String[] splitIntoWords(String text) {
        return text.split("\\s+");
    }

    /**
     * Counts the number of sentences in a text.
     *
     * @param text the text to analyze
     * @return the number of sentences in the text
     * @author Utsav Patel
     */
    public static int countSentences(String text) {
        String[] sentences = text.split("[.!?;:]");
        return sentences.length;
    }
}
