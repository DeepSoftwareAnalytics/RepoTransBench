import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Random;

public class ReorderTest {

    private Reorder reorder;

    @BeforeEach
    public void setUp() {
        Random random = new Random(Settings.seed);
        reorder = new Reorder();
    }

    @Test
    public void testAction() {
        String expectedAction = "netem delay " + reorder.getDelay() + "ms reorder " + (100 - reorder.getReorder()) + "% " + reorder.getCorrelation() + "%";
        assertEquals(expectedAction, reorder.action(), "Action method failed for Reorder");
    }

    @Test
    public void testDesc() {
        String expectedDesc = "reorder after delay of " + reorder.getDelay() + "ms with probability " + (100 - reorder.getReorder()) + " and correlation " + reorder.getCorrelation();
        assertEquals(expectedDesc, reorder.desc(), "Desc method failed for Reorder");
    }

    @Test
    public void testReorderInRange() {
        assertTrue(10 <= reorder.getReorder() && reorder.getReorder() <= 75, "Reorder value out of range for Reorder");
    }

    @Test
    public void testReproducibleReorder() {
        Random random = new Random(Settings.seed);
        Reorder reorder1 = new Reorder();
        random = new Random(Settings.seed);
        Reorder reorder2 = new Reorder();
        assertEquals(reorder1.getReorder(), reorder2.getReorder(), "Reorder value not reproducible for Reorder");
    }

    @Test
    public void testReorderBehavior() {
        assertTrue(reorder instanceof Reorder, "Object is not an instance of Reorder");
        assertTrue(reorder.getDelay() >= 0, "Reorder delay should be non-negative");
        assertTrue(reorder.getDelay() <= 2000, "Reorder delay should not exceed 2000ms");
    }
}
