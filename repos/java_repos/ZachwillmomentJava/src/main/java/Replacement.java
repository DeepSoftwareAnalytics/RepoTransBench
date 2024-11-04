import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Replacement {

    public static LocalDateTime chainCommandsWithReplace(LocalDateTime dateTime, int hour, int minute, int second) {
        return dateTime.withHour(hour).withMinute(minute).withSecond(second);
    }

    public static String chainWithFormat(LocalDateTime dateTime, int hour, int minute, int second, String format) {
        LocalDateTime updated = dateTime.withHour(hour).withMinute(minute).withSecond(second);
        return formatDateTime(updated, format);
    }

    public static String applySuffixFormula(LocalDateTime dateTime, String format) {
        return dateTime.format(DateTimeFormatter.ofPattern(format));
    }

    public static LocalDateTime addWithKeywords(LocalDateTime dateTime, int hours, int minutes, int seconds) {
        return dateTime.plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);
    }

    public static LocalDateTime subtractWithKeywords(LocalDateTime dateTime, int hours, int minutes, int seconds) {
        return dateTime.minusHours(hours).minusMinutes(minutes).minusSeconds(seconds);
    }

    public static String formatDateTime(LocalDateTime dateTime, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return dateTime.format(formatter);
    }
}
