import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


public class WhenTest {

    private Duration oneDay;
    private Duration oneSecond;
    private LocalDate today;
    private LocalDateTime now;
    private ZonedDateTime utc;
    private String timezone;

    @BeforeEach
    public void setUp() {
        When.unsetUtc();
        oneDay = Duration.ofDays(1);
        oneSecond = Duration.ofSeconds(1);
        today = LocalDate.now();
        now = LocalDateTime.now();
        utc = ZonedDateTime.now(Clock.systemUTC());

        String envTimezone = System.getenv("TIMEZONE");
        if (envTimezone != null) {
            timezone = envTimezone;
        } else {
            timezone = "America/New_York";
        }
    }

    @Test
    public void test_addTime() {
        // Test change between months with different number of days
        LocalDateTime testValue = LocalDateTime.of(2012, 3, 31, 0, 0);

        LocalDateTime expectedValue = LocalDateTime.of(2012, 5, 1, 0, 0);
        LocalDateTime result = When.addTime(testValue, 1, ChronoUnit.MONTHS);
        assertEquals(expectedValue, result);

        // Test values going back into February of a leap year
        expectedValue = LocalDateTime.of(2012, 3, 2, 0, 0);
        result = When.addTime(testValue, -1, ChronoUnit.MONTHS);
        assertEquals(expectedValue, result);

        testValue = LocalDateTime.of(2012, 3, 30, 0, 0);
        expectedValue = LocalDateTime.of(2012, 3, 1, 0, 0);
        result = When.addTime(testValue, -1, ChronoUnit.MONTHS);
        assertEquals(expectedValue, result);

        testValue = LocalDateTime.of(2011, 3, 31, 0, 0);
        expectedValue = LocalDateTime.of(2011, 3, 3, 0, 0);
        result = When.addTime(testValue, -1, ChronoUnit.MONTHS);
        assertEquals(expectedValue, result);

        // Test leap day specifically
        testValue = LocalDateTime.of(2012, 2, 29, 0, 0);
        expectedValue = LocalDateTime.of(2013, 3, 1, 0, 0);
        result = When.addTime(testValue, 1, ChronoUnit.YEARS);
        assertEquals(expectedValue, result);

        expectedValue = LocalDateTime.of(2011, 3, 1, 0, 0);
        result = When.addTime(testValue, -1, ChronoUnit.YEARS);
        assertEquals(expectedValue, result);
    }

    @Test
    public void test_addTimeTypeError() {
        // Test IllegalArgumentException raised by When.addTime()
        assertThrows(IllegalArgumentException.class, () -> When.addTime((LocalDateTime) null, 1, ChronoUnit.DAYS));
    }

    @Test
    public void test_isDateType() {
        // Test When.isDateType()
        assertFalse(When.isDateType("a"));
        assertFalse(When.isDateType(1));
        assertFalse(When.isDateType(Arrays.asList("a")));

        assertTrue(When.isDateType(today));
        assertTrue(When.isDateType(now));
        assertTrue(When.isDateType(now.toLocalTime()));
    }

    @Test
    public void test_allTimezones() {
        // Test When.allTimezones()
        // Make sure allTimezones() matches Java's version
        Set<String> allTimezones = When.allTimezones();
        assertEquals(allTimezones, ZoneId.getAvailableZoneIds());
    }

    @Test
    public void test_commonTimezones() {
        // Test When.commonTimezones()
        // Make sure commonTimezones() matches a predefined list
        Set<String> commonTimezones = When.commonTimezones();
        assertEquals(commonTimezones, When.getCommonTimezones());
    }

    @Test
    public void test_ever() {
        // Test When.ever()
        LocalDateTime oldResult = null;
        for (int i = 0; i < 50; i++) {
            LocalDateTime result = When.ever();
            assertNotNull(result);
            assertTrue(result instanceof LocalDateTime);
            assertNotEquals(result, oldResult);
            oldResult = result;
        }
    }

    @Test
    public void test_format() {
        // Test When.format()
        LocalDateTime now = When.now();
        LocalDate today = When.today();
        LocalTime currentTime = now.toLocalTime();

        String[] formatStrings = {"%a", "%A", "%b", "%B", "%c", "%d", "%f", "%H", "%I", "%j",
                                  "%m", "%M", "%p", "%S", "%U", "%w", "%W", "%x", "%X", "%y",
                                  "%Y", "%z", "%Z", "%A, %B %d, %Y %I:%M %p"};

        for (String formatString : formatStrings) {
            // Test date objects
            String builtinDate = DateTimeFormatter.ofPattern(formatString.replace("%", ""), Locale.getDefault()).format(now);
            String resultDate = When.format(now, formatString);
            assertEquals(builtinDate, resultDate);

            // Test datetime objects
            String builtinDatetime = DateTimeFormatter.ofPattern(formatString.replace("%", ""), Locale.getDefault()).format(today);
            String resultDatetime = When.format(today, formatString);
            assertEquals(builtinDatetime, resultDatetime);

            // Test time objects
            String builtinTime = DateTimeFormatter.ofPattern(formatString.replace("%", ""), Locale.getDefault()).format(currentTime);
            String resultTime = When.format(currentTime, formatString);
            assertEquals(builtinTime, resultTime);
        }
    }

    @Test
    public void test_formatTypeError() {
        // Test IllegalArgumentException raised by When.format()
        assertThrows(IllegalArgumentException.class, () -> When.format(LocalDateTime.now(), "%a"));
    }

    @Test
    public void test_how_many_leap_days() {
        // Test When.howManyLeapDays()
    
        // Tests with just years
        LocalDate d1 = LocalDate.of(2012, 1, 1);
        LocalDate d2 = LocalDate.of(2012, 1, 1);
        assertEquals(When.howManyLeapDays(d1, d2), 0);
    
        d2 = LocalDate.of(2013, 1, 1);
        assertEquals(When.howManyLeapDays(d1, d2), 1);
    
        d2 = LocalDate.of(2017, 1, 1);
        assertEquals(When.howManyLeapDays(d1, d2), 2);
    
        // Simple tests using LocalDate
        d2 = LocalDate.of(2012, 2, 1);
        assertEquals(When.howManyLeapDays(d1, d2), 0);
    
        d2 = LocalDate.of(2012, 3, 1);
        assertEquals(When.howManyLeapDays(d1, d2), 1);
    
        d1 = LocalDate.of(2012, 3, 1);
        d2 = LocalDate.of(2012, 4, 1);
        assertEquals(When.howManyLeapDays(d1, d2), 0);
    
        d1 = LocalDate.of(2012, 3, 1);
        d2 = LocalDate.of(2016, 2, 1);
        assertEquals(When.howManyLeapDays(d1, d2), 0);
    
        d1 = LocalDate.of(2012, 3, 1);
        d2 = LocalDate.of(2017, 2, 1);
        assertEquals(When.howManyLeapDays(d1, d2), 1);
    
        // Simple tests using LocalDateTime
        LocalDateTime dt1 = LocalDateTime.of(2012, 2, 28, 0, 0);
        LocalDateTime dt2 = LocalDateTime.of(2012, 2, 29, 0, 0);
        assertEquals(When.howManyLeapDays(dt1.toLocalDate(), dt2.toLocalDate()), 1);
    
        dt1 = LocalDateTime.of(2012, 2, 28, 0, 0);
        dt2 = LocalDateTime.of(2016, 2, 28, 0, 0);
        assertEquals(When.howManyLeapDays(dt1.toLocalDate(), dt2.toLocalDate()), 1);
    
        dt1 = LocalDateTime.of(2012, 2, 28, 0, 0);
        dt2 = LocalDateTime.of(2020, 2, 28, 0, 0);
        assertEquals(When.howManyLeapDays(dt1.toLocalDate(), dt2.toLocalDate()), 2);
    
        dt1 = LocalDateTime.of(2012, 2, 28, 0, 0);
        dt2 = LocalDateTime.of(2020, 2, 29, 0, 0);
        assertEquals(When.howManyLeapDays(dt1.toLocalDate(), dt2.toLocalDate()), 3);
    
        dt1 = LocalDateTime.of(2011, 2, 28, 0, 0);
        dt2 = LocalDateTime.of(2011, 3, 22, 0, 0);
        assertEquals(When.howManyLeapDays(dt1.toLocalDate(), dt2.toLocalDate()), 0);
    
        dt1 = LocalDateTime.of(2012, 2, 28, 0, 0);
        dt2 = LocalDateTime.of(2026, 2, 28, 0, 0);
        assertEquals(When.howManyLeapDays(dt1.toLocalDate(), dt2.toLocalDate()), 4);
    
        // Mixed types
        d1 = LocalDate.of(1970, 1, 1);
        dt2 = LocalDateTime.of(1980, 1, 1, 0, 0);
        assertEquals(When.howManyLeapDays(d1, dt2.toLocalDate()), 2);
    
        dt1 = LocalDateTime.of(1970, 1, 1, 0, 0);
        d2 = LocalDate.of(1990, 1, 1);
        assertEquals(When.howManyLeapDays(dt1.toLocalDate(), d2), 5);
    
        dt1 = LocalDateTime.of(2000, 1, 1, 0, 0);
        d2 = LocalDate.of(3000, 1, 1);
        // At first glance it would appear this should be 250, except that
        // years divisible by 100 are not leap years, of which there are 10,
        // unless they are also divisible by 400. The years 2000, 2400,
        // and 2800 need to be added back in. 250 - (10 - 3) = 243
        assertEquals(When.howManyLeapDays(dt1.toLocalDate(), d2), 243);
    }
    

    @Test
    public void test_how_many_leap_daysTypeError() {
        // Test IllegalArgumentException raised by When.how_many_leap_days()
        LocalDate d1 = When.today();
        LocalDate d2 = When.yesterday();

        // from_date must be valid
        assertThrows(IllegalArgumentException.class, () -> When.howManyLeapDays(d1, d2));
        // to_date must be valid
        assertThrows(IllegalArgumentException.class, () -> When.howManyLeapDays(d1, d2));
    }

    @Test
    public void test_how_many_leapDaysValueError() {
        // Test IllegalArgumentException raised by When.how_many_leap_days()
        LocalDate d1 = When.today();
        LocalDate d2 = When.yesterday();

        // from_date must be before to_date
        assertThrows(IllegalArgumentException.class, () -> When.howManyLeapDays(d1, d2));
    }

    @Test
    public void test_isTimezoneAware() {
        // Test When.isTimezoneAware()
        LocalDateTime naive = When.now();
        ZonedDateTime aware = naive.atZone(ZoneId.of("UTC"));

        assertTrue(When.isTimezoneAware(aware));
        assertFalse(When.isTimezoneAware(naive));

        LocalTime naiveTime = naive.toLocalTime();
        ZonedDateTime awareTime = naiveTime.atDate(LocalDate.now()).atZone(ZoneId.of("UTC"));

        assertTrue(When.isTimezoneAware(awareTime));
        assertFalse(When.isTimezoneAware(naiveTime));
    }

    @Test
    public void test_isTimezoneAwareTypeError() {
        // Test IllegalArgumentException raised by When.isTimezoneAware()
        LocalDate today = When.today();
        assertThrows(IllegalArgumentException.class, () -> When.isTimezoneAware(today));
    }

    @Test
    public void test_isTimezoneNaive() {
        // Test When.isTimezoneNaive()
        LocalDateTime naive = When.now();
        ZonedDateTime aware = naive.atZone(ZoneId.of("UTC"));

        assertTrue(When.isTimezoneNaive(naive));
        assertFalse(When.isTimezoneNaive(aware));

        LocalTime naiveTime = naive.toLocalTime();
        ZonedDateTime awareTime = naiveTime.atDate(LocalDate.now()).atZone(ZoneId.of("UTC"));

        assertTrue(When.isTimezoneNaive(naiveTime));
        assertFalse(When.isTimezoneNaive(awareTime));
    }

    @Test
    public void test_isTimezoneNaiveTypeError() {
        // Test IllegalArgumentException raised by When.isTimezoneNaive()
        LocalDate today = When.today();
        assertThrows(IllegalArgumentException.class, () -> When.isTimezoneNaive(today));
    }

    @Test
    public void test_now() {
        // Test When.now()
        LocalDateTime now = When.now();
        LocalDateTime utc = When.now();

        // Unfortunately the clock keeps ticking each time we capture a value
        // for now so we can't do a direct comparison with assertEquals.
        // It's probably safe to assume the now function is working as long as
        // the difference is less than a second. There's probably a better way
        // to test this, but for now it's sufficient.
        assertTrue(Duration.between(this.now, now).getSeconds() < 1);
        assertTrue(Duration.between(this.utc, utc).getSeconds() < 1);
    }

    @Test
    public void test_setUtc() {
        // Test When.setUtc()
        When.setUtc();
        assertTrue(When.isUtcSet());
    }

    @Test
    public void test_shift() {
        // Test When.shift()
        ZonedDateTime utcZoned = utc;
        ZonedDateTime first = When.shift(utcZoned, "UTC", "America/New_York");
        ZonedDateTime second = When.shift(first, "America/New_York", "UTC");

        assertNotEquals(first, second);
        assertNotEquals(first, utcZoned);
        assertEquals(second, utcZoned);

        // Local time
        ZonedDateTime nowZoned = now.atZone(ZoneId.systemDefault());
        if (timezone.equals("UTC") || timezone.equals("Etc/UTC")) {
            // This block is needed for tests run in an environment set to UTC.
            first = When.shift(nowZoned, null, "America/New_York");
            second = When.shift(first, "America/New_York", null);
        } else {
            first = When.shift(nowZoned, null, "UTC");
            second = When.shift(first, "UTC", null);
        }

        assertNotEquals(first, second);
        assertNotEquals(first, nowZoned);
        assertEquals(second, nowZoned);

        // Set utc parameter to true
        first = When.shift(utcZoned, null, "America/New_York", true);
        second = When.shift(first, "America/New_York", null, true);

        assertNotEquals(first, second);
        assertNotEquals(first, utcZoned);
        assertEquals(second, utcZoned);

        // Force UTC
        When.setUtc();
        first = When.shift(utcZoned, null, "America/New_York");
        second = When.shift(first, "America/New_York", null);

        assertNotEquals(first, second);
        assertNotEquals(first, utcZoned);
        assertEquals(second, utcZoned);
    }

    @Test
    public void test_shiftTypeError() {
        // Test IllegalArgumentException raised by When.shift()
        assertThrows(IllegalArgumentException.class, () -> When.shift((ZonedDateTime) null, null, null));
        assertThrows(IllegalArgumentException.class, () -> When.shift(LocalDateTime.now().atZone(ZoneId.systemDefault()), null, null));
    }

    @Test
    public void test_shiftAware() {
        // Test When.shift() for time zone aware datetimes
        ZonedDateTime nowAware = ZonedDateTime.of(now, ZoneId.of("America/Chicago"));

        // Make sure the datetime's time zone is the respected
        ZonedDateTime first = When.shift(nowAware, null, "America/New_York");
        ZonedDateTime second = When.shift(now.atZone(ZoneId.of("America/Chicago")), "America/Chicago", "America/New_York");

        assertEquals(first, second);

        // Also make sure the from_tz parameter is ignored
        first = When.shift(nowAware, "UTC", "America/New_York");

        assertEquals(first, second);

        // Also make sure the utc parameter is ignored
        first = When.shift(nowAware, null, "America/New_York", true);

        assertEquals(first, second);
    }

    @Test
    public void test_timezone() {
        // Test When.timezone()
        assertEquals(When.timezone(), timezone);
    }

    @Test
    public void test_timezoneObject() {
        // Test When.timezoneObject()
        ZoneId localTimezone = ZoneId.of(timezone);
        assertEquals(When.timezoneObject(), localTimezone);
    }

    @Test
    public void test_today() {
        // Test When.today()
        assertEquals(When.today(), today);
    }

    @Test
    public void test_tomorrow() {
        // Test When.tomorrow()
        assertEquals(When.tomorrow(), today.plusDays(1));
    }

    @Test
    public void test_unsetUtc() {
        // Test When.unsetUtc()
        When.unsetUtc();
        assertFalse(When.isUtcSet());
    }

    @Test
    public void test_yesterday() {
        // Test When.yesterday()
        assertEquals(When.yesterday(), today.minusDays(1));
    }
}
