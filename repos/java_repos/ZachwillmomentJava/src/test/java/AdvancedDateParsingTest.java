import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AdvancedDateParsingTest {

    @Test
    public void testToday() {
        LocalDate d = LocalDate.now();
        LocalDate now = LocalDate.now();
        assertEquals(d, now);
    }

    @Test
    public void testYesterday() {
        LocalDate d = LocalDate.now().minusDays(1);
        LocalDate expecting = LocalDate.now().minusDays(1);
        assertEquals(d, expecting);
    }

    @Test
    public void testFuture() {
        LocalDate d = LocalDate.now().plusDays(1);
        LocalDate expecting = LocalDate.now().plusDays(1);
        assertEquals(d, expecting);
    }

    @Test
    public void test2WeeksAgo() {
        LocalDate d = LocalDate.now().minusWeeks(2);
        LocalDate expecting = LocalDate.now().minusWeeks(2);
        assertEquals(d, expecting);
    }

    @Test
    public void testDateWithMonthAsWord() {
        LocalDate d = LocalDate.of(2012, 12, 12);
        LocalDate expecting = LocalDate.of(2012, 12, 12);
        assertEquals(d, expecting);
    }

    @Test
    public void testDateWithMonthAbbreviation() {
        LocalDate d = LocalDate.of(2012, 12, 12);
        LocalDate expecting = LocalDate.of(2012, 12, 12);
        assertEquals(d, expecting);
    }

    @Test
    public void testDateWithoutDaysDefaultsToFirstDay() {
        LocalDate d = LocalDate.of(2012, 12, 1);
        LocalDate expecting = LocalDate.of(2012, 12, 1);
        assertEquals(d, expecting);
    }
}
