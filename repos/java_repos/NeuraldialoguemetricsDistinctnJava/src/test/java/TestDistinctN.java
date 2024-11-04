
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class TestDistinctN {

    @Test
    public void testUnigram() {
        List<String> sentence = Arrays.asList("the", "the", "the", "the", "the");
        assertEquals(0.2, DistinctN.distinctNSentenceLevel(sentence, 1), 1e-5);
        
        sentence = Arrays.asList("the", "the", "the", "the", "cat");
        assertEquals(0.4, DistinctN.distinctNSentenceLevel(sentence, 1), 1e-5);
    }

    @Test
    public void testBigram() {
        List<String> sentence = Arrays.asList("the", "cat", "sat", "on", "the");
        assertEquals(0.8, DistinctN.distinctNSentenceLevel(sentence, 2), 1e-5);
    }

    @Test
    public void testCorpusLevel() {
        List<List<String>> sentences = Arrays.asList(
            Arrays.asList("the", "cat", "sat", "on", "the", "mat"),
            Arrays.asList("mat", "the", "on", "sat", "cat", "the"),
            Arrays.asList("i", "do", "not", "know"),
            Arrays.asList("Sorry", "but", "i", "do", "not", "know")
        );
        
        assertEquals(0.916666, DistinctN.distinctNCorpusLevel(sentences, 1), 1e-5);
        assertEquals(0.8125, DistinctN.distinctNCorpusLevel(sentences, 2), 1e-5);
    }
}
