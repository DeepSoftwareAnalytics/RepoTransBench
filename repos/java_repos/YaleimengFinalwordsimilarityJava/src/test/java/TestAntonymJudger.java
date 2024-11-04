import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import fanyi.anto_Judger.AntonymJudger;

public class TestAntonymJudger {

    private static AntonymJudger judger;

    @BeforeAll
    public static void setUpClass() {
        judger = new AntonymJudger();
    }

    @Test
    public void testReadFan() {
        assertTrue(judger.getFanyi().size() > 0, "Antonym dictionary should not be empty.");
    }

    @Test
    public void testIsAntiPairTrue() {
        String w1 = "有";
        String w2 = "无";
        assertTrue(judger.isAntiPair(w1, w2), w1 + " and " + w2 + " should be recognized as antonyms.");
    }

    @Test
    public void testIsAntiPairFalse() {
        String w1 = "有";
        String w2 = "你";
        assertFalse(judger.isAntiPair(w1, w2), w1 + " and " + w2 + " should not be recognized as antonyms.");
    }
}
