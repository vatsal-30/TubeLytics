
import org.junit.Test;
import services.SentimentAnalyzer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Class that generate tests for the SentimentAnalyzer
 *
 * @author Vatsal Mukeshkumar Ajmeri
 */
public class SentimentAnalyzerTest {

    private final SentimentAnalyzer analyzer = new SentimentAnalyzer();

    /**
     *
     * Method for handling all happy sentiments
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentimentForDescription_HappySentiment() {
        String description = "satisfied,";
        assertEquals(":-)", analyzer.analyzeSentimentForDescription(description));
    }

    /**
     *
     * Method for handling all sad sentiments
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentimentForDescription_SadSentiment() {
        String description = "hopelessness";
        assertEquals(":-(", analyzer.analyzeSentimentForDescription(description));
    }

    /**
     *
     * Method for handling all neutral sentiments
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentimentForDescription_NeutralSentiment() {
        String description = "It was a day with mixed feelings.";
        assertEquals(":-|", analyzer.analyzeSentimentForDescription(description));
    }

    /**
     *
     * Method for handling when no input is given
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentimentForDescription_NoWords() {
        String description = "";
        assertEquals(":-|", analyzer.analyzeSentimentForDescription(description));
    }

    /**
     *
     * Method for handling data having less happy sentiments and more sad sentiments
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentimentForDescription_LowHappyPercentage() {
        String description = "merrily tears cries vulnerably";
        assertEquals(":-(", analyzer.analyzeSentimentForDescription(description));
    }

    /**
     *
     * Method for handling data having more happy sentiments and less sad sentiments
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentimentForDescription_LowSadPercentage() {
        String description = "Cheer Rejuvenated Delight Grateful brightest funnest sad";
        assertEquals(":-)", analyzer.analyzeSentimentForDescription(description));
    }

    /**
     *
     * Method for handling when no input is given
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void emptyTest() {
        assertEquals(":-|", analyzer.analyzeSentimentForDescription(""));
    }

    /**
     *
     * Method for handling when the entire list has happy sentiments
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentiment_AllHappySentiments() {
        List<String> descriptions = Arrays.asList(
                "happy",
                "Joyful",
                "happy",
                "celebration",
                "luckily",
                "Blissful"
        );
        assertEquals(":-)", analyzer.analyzeSentiment(descriptions));
    }

    /**
     *
     * Method for handling when the entire list has sad sentiments
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentiment_AllSadSentiments() {
        List<String> descriptions = Arrays.asList(
                "sad",
                "unfortunate",
                "depressing"
        );
        assertEquals(":-(", analyzer.analyzeSentiment(descriptions));
    }

    /**
     *
     * Method for handling when the entire list has neutral sentiments
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentiment_AllNeutralSentiments() {
        List<String> descriptions = Arrays.asList(
                "It was a day",
                "Just an ordinary time",
                "No emotions here"
        );
        assertEquals(":-|", analyzer.analyzeSentiment(descriptions));
    }

    /**
     *
     * Method for handling when the entire list has mixed sentiments
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentiment_MixedSentiments() {
        List<String> descriptions = Arrays.asList(
                "positivity",
                "hopelessness"
        );
        assertEquals(":-|", analyzer.analyzeSentiment(descriptions));
    }

    /**
     *
     * Method for handling when the entire list is empty
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentiment_EmptyList() {
        List<String> descriptions = Collections.emptyList();
        assertEquals(":-|", analyzer.analyzeSentiment(descriptions));
    }

    /**
     *
     * Method for handling when the majority of list has happy sentiments
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentiment_MajorityHappySentiment() {
        List<String> descriptions = Arrays.asList(
                "pleasure", "blissfulness", "amazing", "gleeful", "euphoric", "miserableness", "red"
        );
        assertEquals(":-)", analyzer.analyzeSentiment(descriptions));
    }

    /**
     *
     * Method for handling when the majority of list has sad sentiments
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentiment_MajoritySadSentiment() {
        List<String> descriptions = Arrays.asList(
                "downed", "heartbreak", "sorrow", "anguishing", "distressed", "enthusiasm", "blue"
        );
        assertEquals(":-(", analyzer.analyzeSentiment(descriptions));
    }

    /**
     *
     * Method for handling when the majority of list has neutral sentiments with minor sad sentiments
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentiment_MajorityNeutralSadMinorSentiment() {
        List<String> descriptions = Arrays.asList(
                "loneliest", "Exuberantly", "Thrill", "mobile", "go", "red", "blue"
        );
        assertEquals(":-|", analyzer.analyzeSentiment(descriptions));
    }

    /**
     *
     * Method for handling when the majority of list has neutral sentiments with minor happy sentiments
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentiment_MajorityNeutralHappyMinorSentiment() {
        List<String> descriptions = Arrays.asList(
                "loneliest", "gloominess", "Hopeful", "mobile", "go", "red", "blue"
        );
        assertEquals(":-|", analyzer.analyzeSentiment(descriptions));
    }

    /**
     *
     * Method for handling when the majority of list has neutral sentiments with average happy sentiments and a few sad sentiments
     * @author Vatsal Mukeshkumar Ajmeri
     */
    @Test
    public void testAnalyzeSentiment_MajorityNeutralHappyAverageSadMinorSentiment() {
        List<String> descriptions = Arrays.asList(
                "lonelier", "positivity", "satisfaction", "fun", "enthusiastically", "radiant", "merrily", "mobile", "go", "red", "blue", "orange", "at", "the"
        );
        assertEquals(":-|", analyzer.analyzeSentiment(descriptions));
    }
}
