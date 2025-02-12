package actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import model.Response;

import java.util.*;
import java.util.regex.Pattern;

/**
 * The class analyses the sentiments and displays the categorises into 3 categories
 *
 * @author Vatsal Mukeshkumar Ajmeri
 */
public class SentimentAnalyzerActor extends AbstractActor {

    private static final Set<String> happyWords = new HashSet<>(Arrays.asList(
            "happy", "happier", "happiest", "happily", "happiness", "happifying",
            "joy", "joyful", "joyfully", "joyous", "joyousness", "enjoy", "enjoyable",
            "enjoyed", "enjoying", "enjoyment", "excite", "excited", "exciting", "excitedly",
            "excitingly", "excitable", "love", "loved", "loving", "loves", "lovable", "lovingly",
            "lover", "loved-up", "cheer", "cheerful", "cheerfully", "cheerfulness", "cheering",
            "cheered", "cheerier", "cheeriest", "great", "greater", "greatest", "greatly", "greatness",
            "greater-than", "amazing", "amazed", "amazingly", "amazement", "amazingness", "amaze",
            "content", "contented", "contentedly", "contentment", "contenting", "bliss", "blissful",
            "blissfully", "blissfulness", "elated", "elating", "elation", "elatedly", "pleased", "pleasedly",
            "pleasing", "pleases", "pleasingly", "pleasure", "excitement", "excitingly", "excitedness", "gratified",
            "gratify", "gratification", "gratifying", "gratifiedly", "euphoria", "euphoric", "euphorically", "euphorias",
            "delight", "delightful", "delightfully", "delighted", "delighting", "delightfulness", "merry", "merrier",
            "merriest", "merrily", "merriment", "optimistic", "optimistically", "optimism", "optimist", "positive",
            "positively", "positivity", "positive-thinking", "grateful", "gratefully", "gratitude", "gratefulness",
            "grateful-hearted", "radiant", "radiance", "radiantly", "radiating", "satisfy", "satisfied", "satisfying",
            "satisfaction", "satisfiedly", "satisfactorily", "gleeful", "gleefully", "gleefulness", "lively", "livelier",
            "liveliest", "liveliness", "thrilled", "thrilling", "thrill", "thrillingly", "thrilledly", "sunshine",
            "sunshiney", "sunshiny", "sunny", "sunniest", "jovial", "jovially", "jovialness", "bright", "brighter",
            "brightest", "brightness", "brightly", "successful", "successfully", "success", "successes", "lucky",
            "luckier", "luckiest", "luckily", "fun", "funner", "funnest", "fun-loving", "playful", "playfully",
            "playfulness", "playfulnesses", "enthusiastic", "enthusiastically", "enthusiasm", "enthusiast"
    ));

    private static final Set<String> sadWords = new HashSet<>(Arrays.asList(
            "sad", "sadness", "sadly", "saddened", "saddening",
            "unhappy", "unhappily", "unhappiness",
            "depressed", "depressing", "depression", "depressingly",
            "down", "downward", "downer", "downed", "downing",
            "miserable", "miserably", "misery", "miserableness",
            "heartbroken", "heartbreak", "heartbrokenly", "heartbreaking",
            "lonely", "lonelier", "loneliest", "loneliness", "lonesome", "lonesomely",
            "angry", "angrily", "anger", "angering", "angered",
            "hopeless", "hopelessly", "hopelessness",
            "sorrow", "sorrowful", "sorrowfully", "sorrowing", "sorrowed",
            "gloom", "gloomy", "gloomily", "gloominess", "glooming", "gloomed",
            "regret", "regretted", "regrettable", "regretful", "regretfully", "regretting",
            "frustrated", "frustrating", "frustration", "frustratedly",
            "despair", "despaired", "despairing", "desperation", "desperate", "desperately",
            "anguish", "anguished", "anguishing", "anguishly",
            "melancholy", "melancholic", "melancholically",
            "distressed", "distressing", "distressingly", "distressedly",
            "worried", "worrying", "worry", "worryingly",
            "grief", "grieved", "grieving", "grief-stricken", "grievingly",
            "dismay", "dismayed", "dismaying", "dismayingly",
            "dejected", "dejection", "dejectedly", "dejecting",
            "defeated", "defeat", "defeating", "defeatedly",
            "bitter", "bitterly", "bitterness", "bittered",
            "despondent", "despondently", "despondency",
            "downcast", "downcasting", "downcasted",
            "unfortunate", "unfortunely", "unfortunateness",
            "broke", "breaking", "broken",
            "confused", "confusing", "confusion", "confusedly",
            "isolated", "isolation", "isolatedly",
            "embittered", "embittering", "embitteredly",
            "tragically", "tragedy", "traged",
            "empty", "emptier", "emptiest", "emptiness",
            "regretful", "regretfully", "regretting",
            "painful", "painfully", "pain", "pained", "paining",
            "shattered", "shattering", "shatteredness", "shatteringly",
            "hopeless", "hopelessness", "hopelessly",
            "unfortunate", "unfortunates", "unfortuitously",
            "crying", "cry", "cried", "cries", "crying", "cryingly",
            "lament", "lamented", "lamenting", "lamentably", "lamentation",
            "vulnerable", "vulnerability", "vulnerably",
            "sick", "sickly", "sicker", "sickened", "sickening", "sickeningly",
            "tearful", "tearfully", "tears", "teared", "tearfully",
            "disappointed", "disappointing", "disappointingly", "disappointment",
            "shame", "shameful", "shamefully", "shamed",
            "frustration", "frustrated", "frustrating", "frustratingly",
            "weak", "weaker", "weakness", "weakly",
            "crushed", "crushing", "crushedly", "crush",
            "insecure", "insecurity", "insecurities", "insecurely",
            "pessimistic", "pessimism", "pessimistically", "pessimist"
    ));

    private static final Set<String> wordsToRemove = new HashSet<>(Arrays.asList("about", "above", "across", "after", "against", "along", "among", "around", "at", "before", "behind", "below", "beneath", "beside", "between", "beyond", "by", "down", "during", "for", "from", "in", "inside", "into", "like", "near", "of", "off", "on", "onto", "outside", "over", "past", "since", "through", "throughout", "to", "toward", "under", "underneath", "until", "up", "upon", "with", "within", "without", "although", "and", "as", "because", "before", "but", "even if", "even though", "if", "nor", "not only", "but also", "once", "or", "since", "so", "than", "that", "then", "though", "unless", "until", "when", "whenever", "where", "wherever", "whether", "while", "yet", "all", "anybody", "anyone", "anything", "each", "each other", "either", "everybody", "everyone", "everything", "few", "he", "her", "hers", "herself", "him", "himself", "his", "i", "it", "its", "itself", "many", "me", "mine", "myself", "neither", "nobody", "none", "no one", "nothing", "one", "one another", "ours", "ourselves", "she", "some", "somebody", "someone", "something", "that", "their", "theirs", "them", "themselves", "these", "they", "this", "those", "us", "we", "what", "whatever", "when", "where", "which", "who", "whom", "whose", "you", "yours", "yourself", "yourselves", "ah", "aha", "alas", "bravo", "ew", "hey", "hmm", "hurray", "oh", "oops", "ouch", "phew", "ugh", "wow", "yay", "yikes"));


    private static final Pattern NON_ALPHABET_PATTERN = Pattern.compile("[^a-zA-Z]");


    public static Props props() {
        return Props.create(SentimentAnalyzerActor.class);
    }


    public static String analyzeSentimentForDescription(String description) {
        String[] words = NON_ALPHABET_PATTERN.matcher(description.toLowerCase()).replaceAll(" ").split("\\s+");

        long happyCount = Arrays.stream(words)
                .filter(happyWords::contains)
                .count();

        long sadCount = Arrays.stream(words)
                .filter(sadWords::contains)
                .count();

        long removedWords = Arrays.stream(words)
                .filter(wordsToRemove::contains)
                .count();

        long totalWords = words.length - removedWords;

        double happyPercentage = (double) happyCount / totalWords;
        double sadPercentage = (double) sadCount / totalWords;

        if (happyPercentage > 0.7) {
            return ":-)";
        } else if (sadPercentage > 0.7) {
            return ":-(";
        } else {
            return ":-|";
        }
    }

    /**
     * This method runs on the descriptions of all videos and calculates the overall sentiment of the video search results
     *
     * @author Vatsal Mukeshkumar Ajmeri
     */
    public static String analyzeSentiment(List<String> descriptions) {

        if (descriptions.isEmpty()) {
            return ":-|";
        }

        long happyCount = descriptions.stream()
                .map(SentimentAnalyzerActor::analyzeSentimentForDescription)
                .filter(s -> s.equals(":-)"))
                .count();

        long sadCount = descriptions.stream()
                .map(SentimentAnalyzerActor::analyzeSentimentForDescription)
                .filter(s -> s.equals(":-("))
                .count();

        long neutralCount = descriptions.stream()
                .map(SentimentAnalyzerActor::analyzeSentimentForDescription)
                .filter(s -> s.equals(":-|"))
                .count();

        long totalCount = happyCount + sadCount + neutralCount;

        double happyPercentage = (double) happyCount / totalCount;
        double sadPercentage = (double) sadCount / totalCount;
        double neutralPercentage = (double) neutralCount / totalCount;

        if (happyPercentage == sadPercentage) {
            return ":-|";
        } else {
            if (happyPercentage > sadPercentage) {
                if (happyPercentage > neutralPercentage) {
                    return ":-)";
                } else {
                    return ":-|";
                }
            } else {
                if (sadPercentage > neutralPercentage) {
                    return ":-(";
                } else {
                    return ":-|";
                }
            }
        }
    }

    @Override
    public Receive createReceive() {

        return receiveBuilder().match(Response.class, response -> {

            List<String> descriptions = new ArrayList<>();

            response.getVideos().stream()
                    .forEach(video -> {
                        descriptions.add(video.getDescription());
                    });

            String sentiment = analyzeSentiment(descriptions);
            response.setSentiment(sentiment);
            getSender().tell(response, getSelf());
        }).build();
    }
}
