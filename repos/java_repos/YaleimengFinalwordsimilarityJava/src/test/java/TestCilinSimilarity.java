import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import cilin.V3.ciLin.CilinSimilarity;

public class TestCilinSimilarity {

    private static CilinSimilarity similarity;

    @BeforeAll
    public static void setUpClass() {
        similarity = new CilinSimilarity();
    }

    @Test
    public void testReadCilin() {
        assertTrue(similarity.getCodeWord().size() > 0, "code_word dictionary should not be empty.");
        assertTrue(similarity.getWordCode().size() > 0, "word_code dictionary should not be empty.");
        assertTrue(similarity.getVocab().size() > 0, "vocab set should not be empty.");
        assertTrue(similarity.getMydict().size() > 0, "mydict dictionary should not be empty.");
    }

    @Test
    public void testGetCommonStr() {
        assertEquals("今天是星期", similarity.getCommonStr("今天是星期天", "今天是星期六"));
        assertEquals("", similarity.getCommonStr("昨日秋风", "生日快乐"));
    }

    @Test
    public void testInfoContent() {
        String concept = "Aa01";
        double infoContent = similarity.InfoContent(concept);
        assertTrue(infoContent >= 0, "Info_Content should be non-negative.");
    }

    @Test
    public void testSimByIC() {
        String c1 = "Aa01A01=";
        String c2 = "Aa01A02=";
        double sim = similarity.simByIC(c1, c2);
        assertTrue(sim >= 0, "Similarity score should be non-negative.");
    }

    @Test
    public void testSim2018() {
        String w1 = "苹果";
        String w2 = "梨";
        double sim = similarity.sim2018(w1, w2);
        assertTrue(sim >= 0 && sim <= 1, "Similarity score should be between 0 and 1.");
    }
}
