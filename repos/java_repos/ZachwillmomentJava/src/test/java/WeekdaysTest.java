import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.DayOfWeek;

public class WeekdaysTest {

    @Test
    public void testWeekdaysCanBeManipulated() {
        LocalDate d = LocalDate.of(2012, 12, 19);
        LocalDate yesterday = LocalDate.of(2012, 12, 18);
        assertEquals(d.getDayOfWeek().getValue(), 3);
        assertEquals(d.with(java.time.DayOfWeek.WEDNESDAY), d);
        assertEquals(d.with(java.time.DayOfWeek.TUESDAY), yesterday);
    }

    @Test
    public void testWeekAdditionEqualsWeekdayManipulation() {
        LocalDate d = LocalDate.of(2012, 12, 19);
        LocalDate upcoming = Weekdays.addWeeks(d, 1);
        LocalDate expecting = LocalDate.of(2012, 12, 26);
        assertEquals(upcoming, expecting);
    }

    @Test
    public void testWeekdaysWithZeros() {
        LocalDate d = LocalDate.of(2012, 12, 19);
        LocalDate sunday = LocalDate.of(2012, 12, 23);
        assertEquals(Weekdays.withDayOfWeek(d, DayOfWeek.SUNDAY), sunday);
    }

    @Test
    public void testWeekdaysWithNegativeNumbers() {
        LocalDate d = LocalDate.of(2012, 12, 19);
        LocalDate expecting = LocalDate.of(2012, 12, 12);
        assertEquals(Weekdays.minusWeeks(d, 1), expecting);
    }

    @Test
    public void testWeekdaysWithLargerNumberIntoNewYear() {
        LocalDate d = LocalDate.of(2012, 12, 19);
        LocalDate expecting = LocalDate.of(2013, 1, 9);
        assertEquals(d.plusWeeks(3), expecting);
    }
}
