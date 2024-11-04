import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

public class ReplacementTest {

    @Test
    public void testSimpleChainingCommands() {
        LocalDateTime d = LocalDateTime.of(2012, 12, 18, 0, 0, 0);
        LocalDateTime expecting = LocalDateTime.of(2012, 12, 18, 1, 2, 3);
        d = d.withHour(1).withMinute(2).withSecond(3);
        assertEquals(d, expecting);
    }

    @Test
    public void testChainingWithFormat() {
        LocalDateTime d = LocalDateTime.of(2012, 12, 18, 0, 0, 0);
        d = d.withHour(1).plusMinutes(2).withSecond(3);
        String expecting = "2012-12-18 01:02:03";
        assertEquals(d.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), expecting);
    }

    @Test
    public void testSuffixFormula() {
        LocalDateTime d = LocalDateTime.of(2012, 12, 18, 0, 0, 0);
        String expecting = "December 18th, 2012";
        assertEquals(d.format(java.time.format.DateTimeFormatter.ofPattern("MMMM dd'th', yyyy")), expecting);
    }

    @Test
    public void testPropertiesAfterChaining() {
        LocalDateTime d = LocalDateTime.now().withYear(1984).withMonth(1).withDayOfMonth(1);
        assertEquals(d.getYear(), 1984);
    }

    @Test
    public void testAddWithKeywords() {
        LocalDateTime d = LocalDateTime.of(2012, 12, 19, 0, 0, 0);
        d = d.plusHours(1).plusMinutes(2).plusSeconds(3);
        LocalDateTime expecting = LocalDateTime.of(2012, 12, 19, 1, 2, 3);
        assertEquals(d, expecting);
    }

    @Test
    public void testSubtractWithKeywords() {
        LocalDateTime d = LocalDateTime.of(2012, 12, 19, 1, 2, 3);
        d = d.minusHours(1).minusMinutes(2).minusSeconds(3);
        LocalDateTime expecting = LocalDateTime.of(2012, 12, 19, 0, 0, 0);
        assertEquals(d, expecting);
    }

    @Test
    public void testSubtractAMonth() {
        LocalDateTime d = LocalDateTime.of(2020, 1, 1, 0, 0);
        d = d.minusMonths(1);
        LocalDateTime expecting = LocalDateTime.of(2019, 12, 1, 0, 0);
        assertEquals(d, expecting);
    }

    @Test
    public void testSubtractSeveralMonths() {
        LocalDateTime d = LocalDateTime.of(2020, 11, 1, 0, 0);
        d = d.minusMonths(20);
        LocalDateTime expecting = LocalDateTime.of(2019, 3, 1, 0, 0);
        assertEquals(d, expecting);
    }

    @Test
    public void testChainingWithReplaceMethod() {
        LocalDateTime d = LocalDateTime.of(2012, 12, 19, 0, 0, 0);
        d = d.withHour(1).withMinute(2).withSecond(3);
        LocalDateTime expecting = LocalDateTime.of(2012, 12, 19, 1, 2, 3);
        assertEquals(d, expecting);
    }
}
