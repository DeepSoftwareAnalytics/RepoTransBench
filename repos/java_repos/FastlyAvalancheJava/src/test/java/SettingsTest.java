import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.logging.Level;
import java.util.Map;
import java.util.HashMap;
import java.lang.NoSuchFieldException;
public class SettingsTest {

    @Test
    public void testSeedValue() {
        assertEquals(1, Settings.seed, "Seed value incorrect in settings");
    }

    @Test
    public void testDelayValue() {
        assertEquals(1, Settings.delay, "Delay value incorrect in settings");
    }

    @Test
    public void testPFaultValue() {
        assertEquals(0.5, Settings.p_fault, "p_fault value incorrect in settings");
    }

    @Test
    public void testDebugValue() {
        assertFalse(Settings.debug, "Debug value incorrect in settings");
    }

    @Test
    public void testInterfacesValue() {
        assertArrayEquals(new String[]{"eth0"}, Settings.interfaces, "Interfaces value incorrect in settings");
    }

    @Test
    public void testPortsValue() {
        assertArrayEquals(new int[]{2001}, Settings.ports, "Ports value incorrect in settings");
    }

    @Test
    public void testLogLevelValue() {
        assertEquals(Level.INFO, Settings.log_level, "Log level value incorrect in settings");
    }

    @Test
    public void testFaultsValue() {
        Map<Class<?>, Double> expectedFaults = new HashMap<>();
        expectedFaults.put(Partition.class, 0.2);
        expectedFaults.put(PacketLoss.class, 0.2);
        expectedFaults.put(Latency.class, 0.3);
        expectedFaults.put(Reorder.class, 0.3);
        assertEquals(expectedFaults, Settings.faults, "Faults value incorrect in settings");
    }

    @Test
    public void testSettingsIntegrity() throws NoSuchFieldException {
        assertTrue(Settings.class.getField("seed") != null, "Settings missing 'seed' attribute");
        assertTrue(Settings.class.getField("delay") != null, "Settings missing 'delay' attribute");
        assertTrue(Settings.class.getField("p_fault") != null, "Settings missing 'p_fault' attribute");
        assertTrue(Settings.class.getField("debug") != null, "Settings missing 'debug' attribute");
        assertTrue(Settings.class.getField("interfaces") != null, "Settings missing 'interfaces' attribute");
        assertTrue(Settings.class.getField("ports") != null, "Settings missing 'ports' attribute");
        assertTrue(Settings.class.getField("log_level") != null, "Settings missing 'log_level' attribute");
        assertTrue(Settings.class.getField("faults") != null, "Settings missing 'faults' attribute");
    }
}
