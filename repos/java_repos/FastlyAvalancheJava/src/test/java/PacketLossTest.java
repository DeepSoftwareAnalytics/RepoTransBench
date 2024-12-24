import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Random;

public class PacketLossTest {

    private PacketLoss packetLoss;

    @BeforeEach
    public void setUp() {
        Random random = new Random(Settings.seed);
        packetLoss = new PacketLoss();
    }

    @Test
    public void testAction() {
        String expectedAction = "netem loss " + packetLoss.getLoss() + "%";
        assertEquals(expectedAction, packetLoss.action(), "Action method failed for PacketLoss");
    }

    @Test
    public void testDesc() {
        String expectedDesc = "drop packets with probability " + packetLoss.getLoss() + "%";
        assertEquals(expectedDesc, packetLoss.desc(), "Desc method failed for PacketLoss");
    }

    @Test
    public void testLossInRange() {
        assertTrue(5 <= packetLoss.getLoss() && packetLoss.getLoss() <= 10, "Loss value out of range for PacketLoss");
    }

    @Test
    public void testReproducibleLoss() {
        Random random = new Random(Settings.seed);
        PacketLoss packetLoss1 = new PacketLoss();
        random = new Random(Settings.seed);
        PacketLoss packetLoss2 = new PacketLoss();
        assertEquals(packetLoss1.getLoss(), packetLoss2.getLoss(), "Loss value not reproducible for PacketLoss");
    }

    @Test
    public void testPacketLossBehavior() {
        assertTrue(packetLoss instanceof PacketLoss, "Object is not an instance of PacketLoss");
        assertTrue(packetLoss.getLoss() >= 0, "PacketLoss loss should be non-negative");
        assertTrue(packetLoss.getLoss() <= 100, "PacketLoss loss should not exceed 100");
    }
}
