package actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import model.Response;

import java.util.concurrent.atomic.AtomicReference;

public class DescriptionReadabilityActor extends AbstractActor {

    public static Props props() {
        return Props.create(DescriptionReadabilityActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Response.class, response -> {
            calculateDescriptionReadability(response);
            getSender().tell(response, getSelf());
        }).build();
    }

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
     * This method will calculate the readability score.
     * Flesch-Kincaid Grade Level
     * Flesch Reading Score
     *
     * @param description description of video
     * @return double[] - It contains the readability score
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
     * This method will calculate Syllables of the word.
     *
     * @param word work of description
     * @return count of syllables
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
     * This method will check whether it is consonant or not.
     *
     * @param c character
     * @return boolean
     * @author Utsav Patel
     */
    public static boolean isConsonant(char c) {
        return "bcdfghjklmnpqrstvwxyz".indexOf(c) >= 0;
    }

    /**
     * This method will split text into words
     *
     * @param text to split into words
     * @return Array of String
     * @author Utsav Patel
     */
    public static String[] splitIntoWords(String text) {
        return text.split("\\s+");
    }

    /**
     * This method will count number of sentences.
     *
     * @param text to count number of sentences
     * @return int - number of sentences
     * @author Utsav Patel
     */
    public static int countSentences(String text) {
        String[] sentences = text.split("[.!?;:]");
        return sentences.length;
    }
}
