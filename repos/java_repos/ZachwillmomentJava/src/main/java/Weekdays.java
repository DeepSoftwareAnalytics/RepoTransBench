import java.time.DayOfWeek;
import java.time.LocalDate;

public class Weekdays {

    public static LocalDate addWeeks(LocalDate date, int weeks) {
        return date.plusWeeks(weeks);
    }

    public static LocalDate withDayOfWeek(LocalDate date, DayOfWeek dayOfWeek) {
        return date.with(dayOfWeek);
    }

    public static LocalDate minusWeeks(LocalDate date, int weeks) {
        return date.minusWeeks(weeks);
    }

    public static LocalDate plusWeeks(LocalDate date, int weeks) {
        return date.plusWeeks(weeks);
    }
}
