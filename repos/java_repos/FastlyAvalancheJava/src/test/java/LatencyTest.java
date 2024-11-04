import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Random;

public class LatencyTest {

    private Latency latency;

    @BeforeEach
    public void setUp() {
        Random random = new Random(Settings.seed);
        latency = new Latency();
    }

    @Test
    public void testAction() {
        String expectedAction = "netem delay " + latency.getLatency() + "ms";
        assertEquals(expectedAction, latency.action(), "Action method failed for Latency");
    }

    @Test
    public void testDesc() {
        String expectedDesc = "delay of " + latency.getLatency() + "ms";
        assertEquals(expectedDesc, latency.desc(), "Desc method failed for Latency");
    }

    @Test
    public void testLatencyInRange() {
        assertTrue(100 <= latency.getLatency() && latency.getLatency() <= 1000, "Latency value out of range for Latency");
    }

    @Test
    public void testReproducibleLatency() {
        Random random = new Random(Settings.seed);
        Latency latency1 = new Latency();
        random = new Random(Settings.seed);
        Latency latency2 = new Latency();
        assertEquals(latency1.getLatency(), latency2.getLatency(), "Latency value not reproducible for Latency");
    }

    @Test
    public void testLatencyBehavior() {
        assertTrue(latency instanceof Latency, "Object is not an instance of Latency");
        assertTrue(latency.getLatency() >= 0, "Latency should be non-negative");
        assertTrue(latency.getLatency() <= 2000, "Latency should not exceed 2000ms");
    }
}
