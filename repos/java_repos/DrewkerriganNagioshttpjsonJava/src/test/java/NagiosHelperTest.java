import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NagiosHelperTest {

    /**
     * Tests for the NagiosHelper
     */

    @Test
    void testGetCodeDefault() {
        NagiosHelper helper = new NagiosHelper();
        assertEquals(0, helper.getCode());
    }

    @Test
    void testGetCodeWarning() {
        NagiosHelper helper = new NagiosHelper();
        helper.setWarningMessage("foobar");
        assertEquals(1, helper.getCode());
    }

    @Test
    void testGetCodeCritical() {
        NagiosHelper helper = new NagiosHelper();
        helper.setCriticalMessage("foobar");
        assertEquals(2, helper.getCode());
    }

    @Test
    void testGetCodeUnknown() {
        NagiosHelper helper = new NagiosHelper();
        helper.setUnknownMessage("foobar");
        assertEquals(3, helper.getCode());
    }

    @Test
    void testGetMessageDefault() {
        NagiosHelper helper = new NagiosHelper();
        assertEquals("OK: Status OK.", helper.getMessage());
    }

    @Test
    void testGetMessagePerformanceData() {
        NagiosHelper helper = new NagiosHelper();
        helper.setPerformanceData("foobar");
        assertEquals("OK: foobar Status OK. |foobar", helper.getMessage());
    }
}
