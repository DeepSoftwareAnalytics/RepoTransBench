import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class SimpleAPITest {

    @Test
    public void testDateFunctionTakesAString() {
        LocalDate d = LocalDate.parse("2012-12-18");
        assertEquals(d, LocalDate.of(2012, 12, 18));
    }

    @Test
    public void testDateFunctionWithDatetime() {
        LocalDateTime d = LocalDateTime.of(2012, 12, 18, 0, 0);
        assertEquals(d, LocalDateTime.of(2012, 12, 18, 0, 0));
    }

    @Test
    public void testDateFunctionWithIterable() {
        LocalDate d = LocalDate.of(2012, 12, 18);
        assertEquals(d, LocalDate.of(2012, 12, 18));
    }

    @Test
    public void testDateFunctionWithArgs() {
        LocalDate d = LocalDate.of(2012, 12, 18);
        assertEquals(d, LocalDate.of(2012, 12, 18));
    }

    @Test
    public void testDateFunctionWithString() {
        LocalDate d = LocalDate.parse("2012-12-18");
        assertEquals(d, LocalDate.of(2012, 12, 18));
    }

    @Test
    public void testDateFunctionWithUnicode() {
        LocalDate d = LocalDate.parse("2012-12-18");
        assertEquals(d, LocalDate.of(2012, 12, 18));
    }

    @Test
    public void testUtcFunctionWithArgs() {
        ZonedDateTime d = ZonedDateTime.of(2012, 12, 18, 0, 0, 0, 0, ZoneId.of("UTC"));
        assertEquals(d, ZonedDateTime.of(2012, 12, 18, 0, 0, 0, 0, ZoneId.of("UTC")));
    }

    @Test
    public void testNowFunctionWithCurrentDate() {
        LocalDateTime d = LocalDateTime.now();
        LocalDateTime now = LocalDateTime.now();
        assertEquals(d.getYear(), now.getYear());
        assertEquals(d.getMonthValue(), now.getMonthValue());
        assertEquals(d.getDayOfMonth(), now.getDayOfMonth());
        assertEquals(d.getHour(), now.getHour());
        assertEquals(d.getSecond(), now.getSecond());
    }

    @Test
    public void testUtcnowFunction() {
        ZonedDateTime d = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        assertEquals(d.getYear(), now.getYear());
        assertEquals(d.getMonthValue(), now.getMonthValue());
        assertEquals(d.getDayOfMonth(), now.getDayOfMonth());
        assertEquals(d.getHour(), now.getHour());
        assertEquals(d.getSecond(), now.getSecond());
    }

    @Test
    public void testMomentCanTransferBetweenDatetimeAndMoment() {
        LocalDateTime d = LocalDateTime.now();
        assertEquals(SimpleAPI.transferBetweenDatetimeAndMoment(d), LocalDateTime.of(d.getYear(), d.getMonthValue(), d.getDayOfMonth(), d.getHour(), d.getMinute(), d.getSecond()));
    }

    @Test
    public void testMomentUnixCommand() {
        long epochSeconds = 1355788800L;
        ZonedDateTime d = ZonedDateTime.ofInstant(new Date(epochSeconds * 1000).toInstant(), ZoneId.of("UTC"));
        ZonedDateTime expected = ZonedDateTime.of(2012, 12, 18, 0, 0, 0, 0, ZoneId.of("UTC"));
        assertEquals(d, expected);
    }

    @Test
    public void testMomentCanSubtractAnotherMoment() {
        LocalDate d = LocalDate.of(2012, 12, 19);
        assertTrue(d.minusDays(1).isBefore(d));
    }

    @Test
    public void testMomentCanSubtractADatetime() {
        LocalDate d = LocalDate.of(2012, 12, 19);
        assertTrue(d.minusDays(1).isBefore(d));
    }

    @Test
    public void testADatetimeCanSubtractAMoment() {
        LocalDate d = LocalDate.of(2012, 12, 18);
        assertTrue(LocalDate.of(2012, 12, 19).isAfter(d));
    }

    @Test
    public void testDateProperty() {
        LocalDate d = LocalDate.of(2012, 12, 18);
        assertEquals(d, LocalDate.of(2012, 12, 18));
    }

    @Test
    public void testZeroProperty() {
        LocalDateTime d = LocalDateTime.of(2012, 12, 18, 1, 2, 3);
        assertEquals(d.withHour(0).withMinute(0).withSecond(0), LocalDateTime.of(2012, 12, 18, 0, 0));
    }

    @Test
    public void testCloningAUTCDate() {
        ZonedDateTime utc = ZonedDateTime.parse("2016-01-13T00:00:00Z");
        assertEquals(utc.getHour(), 0);
        assertEquals(utc.toLocalDate().toString(), "2016-01-13");
        ZonedDateTime usa = utc.withZoneSameInstant(ZoneId.of("US/Eastern"));
        assertEquals(usa.getHour(), 19);
        assertEquals(usa.toLocalDate().toString(), "2016-01-12");
    }

    @Test
    public void testCopyMethodIsSameAsClone() {
        LocalDateTime d = LocalDateTime.of(2016, 5, 21, 0, 0);
        LocalDateTime copy = d.minusWeeks(1);
        assertEquals(d, LocalDateTime.of(2016, 5, 21, 0, 0));
        assertEquals(copy, LocalDateTime.of(2016, 5, 14, 0, 0));
    }
}
