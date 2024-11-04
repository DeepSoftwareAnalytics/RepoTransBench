import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DistinctN {

    public static double distinctNSentenceLevel(List<String> sentence, int n) {
        if (sentence.size() == 0) {
            return 0.0; // Prevent a zero division
        }
        Set<List<String>> distinctNGrams = new HashSet<>(ngrams(sentence, n));
        return (double) distinctNGrams.size() / sentence.size();
    }

    public static double distinctNCorpusLevel(List<List<String>> sentences, int n) {
        double sum = 0.0;
        for (List<String> sentence : sentences) {
            sum += distinctNSentenceLevel(sentence, n);
        }
        return sum / sentences.size();
    }

    private static List<List<String>> ngrams(List<String> sequence, int n) {
        List<List<String>> ngrams = new java.util.ArrayList<>();
        for (int i = 0; i <= sequence.size() - n; i++) {
            ngrams.add(sequence.subList(i, i + n));
        }
        return ngrams;
    }
}
