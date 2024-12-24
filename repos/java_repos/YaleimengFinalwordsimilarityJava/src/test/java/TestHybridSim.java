import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import Hybrid_Sim.HybridSim;

public class TestHybridSim {

    private static HybridSim hybridSim;

    @BeforeAll
    public static void setUpClass() {
        hybridSim = new HybridSim();
    }

    @Test
    public void testGetMiddleSimCommon() {
        String w1 = "苹果";
        String w2 = "梨";
        double sim = hybridSim.getMiddleSim(w1, w2);
        assertTrue(sim >= 0 && sim <= 1, "Similarity should be between 0 and 1.");
    }

    @Test
    public void testGetMiddleSimOnlyHownet() {
        String w1 = "电子";
        String w2 = "计算机";
        double sim = hybridSim.getMiddleSim(w1, w2);
        assertTrue(sim >= 0 && sim <= 1, "Similarity should be between 0 and 1.");
    }

    @Test
    public void testGetMiddleSimOnlyCilin() {
        String w1 = "朋友";
        String w2 = "伙伴";
        double sim = hybridSim.getMiddleSim(w1, w2);
        assertTrue(sim >= 0 && sim <= 1, "Similarity should be between 0 and 1.");
    }

    @Test
    public void testGetFinalSimAntonym() {
        String w1 = "你好";
        String w2 = "今天";
        double sim = hybridSim.getFinalSim(w1, w2);
        assertTrue(sim >= -1 && sim <= 1, "Similarity should be between -1 and 1.");
    }

    @Test
    public void testGetFinalSimNormal() {
        String w1 = "苹果";
        String w2 = "香蕉";
        double sim = hybridSim.getFinalSim(w1, w2);
        assertTrue(sim >= 0 && sim <= 1, "Similarity should be between 0 and 1.");
    }
}
