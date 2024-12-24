import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GarminConnectExporterTest {

    @Test
    public void testFormatDate() {
        Date date = new Date(118, 2, 8, 12, 23, 22); // 2018-03-08 12:23:22
        String formattedDate = GarminConnectExporter.formatDate(date);
        assertEquals("2018-03-08", formattedDate);
    }

    @Test
    public void testPaceOrSpeedRawCycling() {
        assertEquals(36.0, GarminConnectExporter.paceOrSpeedRaw(2, 4, 10.0));
    }

    @Test
    public void testPaceOrSpeedRawRunning() {
        assertEquals(5.0, GarminConnectExporter.paceOrSpeedRaw(1, 4, 10.0 / 3));
    }

    @Test
    public void testPaceOrSpeedFormattedCycling() {
        assertEquals("36.0", GarminConnectExporter.paceOrSpeedFormatted(2, 4, 10.0));
    }

    @Test
    public void testPaceOrSpeedFormattedRunning() {
        assertEquals("05:00", GarminConnectExporter.paceOrSpeedFormatted(1, 4, 10.0 / 3));
    }

    @Test
    public void testTrunc6More() {
        assertEquals("0.123456", GarminConnectExporter.trunc6(0.123456789));
    }

    @Test
    public void testTrunc6Less() {
        assertEquals("0.123000", GarminConnectExporter.trunc6(0.123));
    }
}
