import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SimpleAPI {

    public static LocalDate parseDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static LocalDateTime parseDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public static LocalDate parseDateFromList(List<Integer> dateComponents) {
        return LocalDate.of(dateComponents.get(0), dateComponents.get(1), dateComponents.get(2));
    }

    public static ZonedDateTime utc(int year, int month, int day, int hour, int minute, int second) {
        return ZonedDateTime.of(year, month, day, hour, minute, second, 0, ZoneId.of("UTC"));
    }

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    public static ZonedDateTime utcNow() {
        return ZonedDateTime.now(ZoneId.of("UTC"));
    }

    public static LocalDateTime transferBetweenDatetimeAndMoment(LocalDateTime dateTime) {
        return LocalDateTime.of(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(), dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond());
    }

    public static ZonedDateTime unixToZonedDateTime(long epochSeconds) {
        return ZonedDateTime.ofInstant(java.util.Date.from(java.time.Instant.ofEpochSecond(epochSeconds)).toInstant(), ZoneId.of("UTC"));
    }
}
