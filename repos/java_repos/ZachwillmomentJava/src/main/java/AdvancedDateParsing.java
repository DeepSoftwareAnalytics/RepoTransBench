import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class AdvancedDateParsing {

    public static LocalDate today() {
        return LocalDate.now();
    }

    public static LocalDate yesterday() {
        return LocalDate.now().minusDays(1);
    }

    public static LocalDate future(int days) {
        return LocalDate.now().plusDays(days);
    }

    public static LocalDate dateWithMonthAndDay(int year, int month, int day) {
        return LocalDate.of(year, month, day);
    }

    public static LocalDate dateWithoutDay(int year, int month) {
        return LocalDate.of(year, month, 1);
    }

    public static LocalDate parseDate(String date, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return LocalDate.parse(date, formatter);
    }
}
