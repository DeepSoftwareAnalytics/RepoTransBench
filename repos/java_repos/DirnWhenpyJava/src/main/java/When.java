import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;

public class When {
    private static boolean FORCE_UTC = false;

    public static LocalDateTime addTime(LocalDateTime value, int amountToAdd, TemporalUnit unit) {
        if (value == null) {
            throw new IllegalArgumentException("DateTime value must not be null");
        }
        return value.plus(amountToAdd, unit);
    }

    public static boolean isDateType(Object object) {
        return object instanceof LocalDate || object instanceof LocalDateTime || object instanceof LocalTime;
    }

    public static Set<String> allTimezones() {
        return ZoneId.getAvailableZoneIds();
    }

    public static Set<String> commonTimezones() {
        return getCommonTimezones();
    }

    public static Set<String> getCommonTimezones() {
        return new HashSet<>(Arrays.asList(
            "UTC", "GMT", "US/Eastern", "America/New_York", 
            "US/Central", "America/Chicago", "US/Mountain", 
            "America/Denver", "US/Pacific", "America/Los_Angeles"
        ));
    }

    public static LocalDateTime ever() {
        Random random = new Random();
        int year = LocalDate.now().getYear() + random.nextInt(201) - 100;
        int month = 1 + random.nextInt(12);
        int day = 1 + random.nextInt(YearMonth.of(year, Month.of(month)).lengthOfMonth());
        int hour = random.nextInt(24);
        int minute = random.nextInt(60);
        int second = random.nextInt(60);
        int nanos = random.nextInt(1000000000);
        return LocalDateTime.of(year, month, day, hour, minute, second, nanos);
    }

    public static String format(Temporal temporal, String format) {
        if (format.indexOf('%') != -1) {
            format = format.replace("%a", "EEE").replace("%A", "EEEE").replace("%b", "MMM").replace("%B", "MMMM")
                    .replace("%c", "EEE MMM d HH:mm:ss yyyy").replace("%d", "dd").replace("%f", "SSSSSS")
                    .replace("%H", "HH").replace("%I", "hh").replace("%j", "DDD").replace("%m", "MM")
                    .replace("%M", "mm").replace("%p", "a").replace("%S", "ss").replace("%U", "ww")
                    .replace("%w", "F").replace("%W", "w").replace("%x", "dd/MM/yy").replace("%X", "HH:mm:ss")
                    .replace("%y", "yy").replace("%Y", "yyyy").replace("%z", "Z").replace("%Z", "z");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, Locale.getDefault());
        if (temporal instanceof LocalDateTime) {
            return ((LocalDateTime) temporal).format(formatter);
        } else if (temporal instanceof LocalDate) {
            return ((LocalDate) temporal).format(formatter);
        } else if (temporal instanceof LocalTime) {
            return ((LocalTime) temporal).format(formatter);
        } else {
            throw new IllegalArgumentException("Unsupported Temporal type");
        }
    }

    public static long howManyLeapDays(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from_date must be before to_date");
        }
        int leapYears = 0;
        for (int year = from.getYear(); year <= to.getYear(); year++) {
            if (Year.isLeap(year)) {
                leapYears++;
            }
        }
        return leapYears;
    }

    public static Duration is5Oclock() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime fivePM = LocalTime.of(17, 0);
        LocalDateTime fiveOClock = LocalDateTime.of(now.toLocalDate(), fivePM);
        return Duration.between(now, fiveOClock);
    }

    public static boolean isTimezoneAware(Temporal temporal) {
        if (temporal instanceof ZonedDateTime) {
            return true;
        }
        if (temporal instanceof LocalDateTime || temporal instanceof LocalTime) {
            return ((LocalDateTime) temporal).toLocalDate().equals(ZonedDateTime.now().toLocalDate());
        }
        return false;
    }

    public static boolean isTimezoneNaive(Temporal temporal) {
        return !isTimezoneAware(temporal);
    }

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    public static void setUtc() {
        FORCE_UTC = true;
    }

    public static ZonedDateTime shift(ZonedDateTime dateTime, String fromTz, String toTz) {
        return shift(dateTime, fromTz, toTz, FORCE_UTC);
    }

    public static ZonedDateTime shift(ZonedDateTime dateTime, String fromTz, String toTz, boolean utc) {
        ZonedDateTime adjustedDateTime = dateTime.withZoneSameInstant(FORCE_UTC || utc ? ZoneId.of("UTC") : ZoneId.of(toTz));
        return adjustedDateTime.withZoneSameInstant(ZoneId.of(fromTz));
    }

    public static String timezone() {
        String timezone = System.getenv("TIMEZONE");
        return timezone != null ? timezone : "America/New_York";
    }

    public static ZoneId timezoneObject() {
        return ZoneId.of(timezone());
    }

    public static LocalDate today() {
        return LocalDate.now();
    }

    public static LocalDate tomorrow() {
        return LocalDate.now().plusDays(1);
    }

    public static void unsetUtc() {
        FORCE_UTC = false;
    }

    public static LocalDate yesterday() {
        return LocalDate.now().minusDays(1);
    }
}
