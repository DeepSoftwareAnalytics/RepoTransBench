import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import hownet.howNet.How_Similarity;
import hownet.howNet.GlossaryElement;
import hownet.howNet.SememeElement;
import java.util.List;
public class TestHowSimilarity {

    private static How_Similarity similarity;

    @BeforeAll
    public static void setUpClass() {
        similarity = new How_Similarity();
    }

    @Test
    public void testInit() {
        assertTrue(similarity.getSememetable().size() > 0, "Sememe table should not be empty.");
        assertTrue(similarity.getGlossarytable().size() > 0, "Glossary table should not be empty.");
        assertTrue(similarity.getVocab().size() > 0, "Vocabulary should not be empty.");
    }

    @Test
    public void testLoadSememeTable() {
        assertNotNull(similarity.getSememetable().get("1"), "Sememe table should contain key '1'.");
        assertTrue(similarity.getSememetable().get("1") instanceof SememeElement, "Value should be a SememeElement.");
    }

    @Test
    public void testLoadGlossary() {
        assertNotNull(similarity.getGlossarytable().get("66164\t黯然神伤"), "Glossary table should contain key '66164\t黯然神伤'.");
        assertTrue(similarity.getGlossarytable().get("66164\t黯然神伤") instanceof GlossaryElement, "Value should be a GlossaryElement.");
    }

    @Test
    public void testGetSememeByID() {
        SememeElement sememe = similarity.getSememeByID("1");
        assertTrue(sememe instanceof SememeElement, "Should return a SememeElement.");
        assertEquals("1", sememe.getId(), "Sememe ID should be '1'.");
    }

    @Test
    public void testGetSememeByZh() {
        SememeElement sememe = similarity.getSememeByZh("事件");
        assertTrue(sememe instanceof SememeElement, "Should return a SememeElement.");
        assertEquals("事件", sememe.getSememeZh(), "Sememe Chinese should be '事件'.");
    }

    @Test
    public void testGetGlossary() {
        List<GlossaryElement> glossary = similarity.getGlossary("啊哈");
        assertTrue(glossary.size() > 0, "Glossary list should not be empty.");
    }

    @Test
    public void testCalcSememeSim() {
        double sim = similarity.calcSememeSim("事件", "关系");
        assertTrue(sim >= -1 && sim <= 1, "Similarity should be between -1 and 1.");
    }

    @Test
    public void testCalc() {
        double sim = similarity.calc("苹果", "梨");
        assertTrue(sim >= -2 && sim <= 1, "Similarity should be between -2 and 1.");
    }
}
