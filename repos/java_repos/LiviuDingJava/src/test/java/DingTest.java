import org.junit.Test;
import org.junit.Assert;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DingTest {

    @Test
    public void testTimeParserRelative1s() {
        String[] time = {"1s"};
        int seconds = TimeParser.getSecondsRelative(time);
        Assert.assertEquals(1, seconds);
    }

    @Test
    public void testTimeParserRelative1m() {
        String[] time = {"1m"};
        int seconds = TimeParser.getSecondsRelative(time);
        Assert.assertEquals(60, seconds);
    }

    @Test
    public void testTimeParserRelative1h() {
        String[] time = {"1h"};
        int seconds = TimeParser.getSecondsRelative(time);
        Assert.assertEquals(3600, seconds);
    }

    @Test
    public void testTimeParserRelative1h30m() {
        String[] time = {"1h", "30m"};
        int seconds = TimeParser.getSecondsRelative(time);
        Assert.assertEquals(5400, seconds);
    }

    @Test
    public void testTimeParserRelative1h30m10s() {
        String[] time = {"1h", "30m", "10s"};
        int seconds = TimeParser.getSecondsRelative(time);
        Assert.assertEquals(5410, seconds);
    }

    @Test
    public void testTimeParserAbsolute10s() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in10Seconds = now.plusSeconds(10);
        String timeStr = in10Seconds.toLocalTime().truncatedTo(ChronoUnit.SECONDS).toString();
        int seconds = TimeParser.getSecondsAbsolute(timeStr);
        Assert.assertTrue(Math.abs(seconds - 10) < 2);
    }

    @Test
    public void testTimeParserAbsolute1h() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in1Hour = now.plusHours(1);
        String timeStr = in1Hour.toLocalTime().toString().split("\\.")[0];
        int seconds = TimeParser.getSecondsAbsolute(timeStr);
        Assert.assertTrue(seconds > 0 && seconds < 3600);
    }

    @Test
    public void testTimeParserAbsolute5m() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in5Minutes = now.plusMinutes(5);
        String timeStr = in5Minutes.toLocalTime().toString().split("\\.")[0];
        int seconds = TimeParser.getSecondsAbsolute(timeStr);
        Assert.assertTrue(seconds >= 240 && seconds < 360);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRelativeTimeRegexVeryWrongRegex() {
        DingArgs.validateRelativeTime("this is very wrong");
    }

    @Test
    public void testRelativeTimeRegex1s() {
        DingArgs.validateRelativeTime("1s");
    }

    @Test
    public void testRelativeTimeRegexHMS() {
        DingArgs.validateRelativeTime("12h 12m 34s");
    }

    @Test
    public void testRelativeTimeRegexExtraSpace() {
        DingArgs.validateRelativeTime("12h           12m");
    }

    @Test
    public void testAbsoluteTimeHHMMSS() {
        DingArgs.validateAbsoluteTime("12:12:12");
    }

    @Test
    public void testAbsoluteTimeHH() {
        DingArgs.validateAbsoluteTime("12");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAbsoluteTimeHHMMSSInvalidHour() {
        DingArgs.validateAbsoluteTime("32:12:12");
    }

    @Test
    public void testBeepWithCustomCommand() {
        BeepTester beepTester = new BeepTester();
        beepTester.captureOutput();
        Ding.beep(1, "echo 'test'");
        String output = beepTester.getOutput();
        Assert.assertEquals("test\n".repeat(Ding.N_BEEPS), output);
    }
}

