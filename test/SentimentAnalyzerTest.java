
import org.junit.Test;
import services.SentimentAnalyzer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SentimentAnalyzerTest {

    private final SentimentAnalyzer analyzer = new SentimentAnalyzer();

    // Test cases for analyzeSentimentForDescription method
    @Test
    public void testAnalyzeSentimentForDescription_HappySentiment() {
        String description = "satisfied";
        assertEquals(":-)", analyzer.analyzeSentimentForDescription(description));
    }

    @Test
    public void testAnalyzeSentimentForDescription_SadSentiment() {
        String description = "hopelessness";
        assertEquals(":-(", analyzer.analyzeSentimentForDescription(description));
    }

    @Test
    public void testAnalyzeSentimentForDescription_NeutralSentiment() {
        String description = "It was a day with mixed feelings.";
        assertEquals(":-|", analyzer.analyzeSentimentForDescription(description));
    }

    @Test
    public void testAnalyzeSentimentForDescription_NoWords() {
        String description = "";
        assertEquals(":-|", analyzer.analyzeSentimentForDescription(description));
    }

    @Test
    public void testAnalyzeSentimentForDescription_LowHappyPercentage() {
        String description = "merrily tears cries vulnerably";
        assertEquals(":-(", analyzer.analyzeSentimentForDescription(description));
    }

    @Test
    public void testAnalyzeSentimentForDescription_LowSadPercentage() {
        String description = "Cheer Rejuvenated Delight Grateful brightest funnest sad";
        assertEquals(":-)", analyzer.analyzeSentimentForDescription(description));
    }

    @Test
    public void emptyTest() {
        assertEquals(":-|", analyzer.analyzeSentimentForDescription(""));
    }

    // Test cases for analyzeSentiment method
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

    @Test
    public void testAnalyzeSentiment_AllSadSentiments() {
        List<String> descriptions = Arrays.asList(
                "sad",
                "unfortunate",
                "depressing"
        );
        assertEquals(":-(", analyzer.analyzeSentiment(descriptions));
    }

    @Test
    public void testAnalyzeSentiment_AllNeutralSentiments() {
        List<String> descriptions = Arrays.asList(
                "It was a day",
                "Just an ordinary time",
                "No emotions here"
        );
        assertEquals(":-|", analyzer.analyzeSentiment(descriptions));
    }

    @Test
    public void testAnalyzeSentiment_MixedSentiments() {
        List<String> descriptions = Arrays.asList(
                "positivity",
                "hopelessness"
        );
        assertEquals(":-|", analyzer.analyzeSentiment(descriptions));
    }

    @Test
    public void testAnalyzeSentiment_EmptyList() {
        List<String> descriptions = Collections.emptyList();
        assertEquals(":-|", analyzer.analyzeSentiment(descriptions));
    }

    @Test
    public void testAnalyzeSentiment_MajorityHappySentiment() {
        List<String> descriptions = Arrays.asList(
                "pleasure blissfulness amazing gleeful euphoric miserableness red"
        );
        assertEquals(":-)", analyzer.analyzeSentiment(descriptions));
    }

    @Test
    public void testAnalyzeSentiment_MajoritySadSentiment() {
        List<String> descriptions = Arrays.asList(
                "downed heartbreak sorrow anguishing distressed enthusiasm blue"
        );
        assertEquals(":-(", analyzer.analyzeSentiment(descriptions));
    }

    @Test
    public void testAnalyzeSentiment_MajorityNeutralSadMinorSentiment() {
        List<String> descriptions = Arrays.asList(
                "loneliest Exuberantly Thrill mobile go red blue"
        );
        assertEquals(":-|", analyzer.analyzeSentiment(descriptions));
    }

    @Test
    public void testAnalyzeSentiment_MajorityNeutralHappyMinorSentiment() {
        List<String> descriptions = Arrays.asList(
                "loneliest gloominess Hopeful mobile go red blue"
        );
        assertEquals(":-|", analyzer.analyzeSentiment(descriptions));
    }
}
