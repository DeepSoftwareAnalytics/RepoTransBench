import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class TimeParser {
    private static final Map<String, Integer> TIME_MAP = new HashMap<>();

    static {
        TIME_MAP.put("s", 1);
        TIME_MAP.put("m", 60);
        TIME_MAP.put("h", 3600);
    }

    public static int getSecondsRelative(String[] time) {
        int seconds = 0;
        for (String t : time) {
            String unit = t.substring(t.length() - 1);
            int value = Integer.parseInt(t.substring(0, t.length() - 1));
            seconds += TIME_MAP.get(unit) * value;
        }
        return seconds;
    }

    public static int getSecondsAbsolute(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm[:ss]");
        LocalTime userTime = LocalTime.parse(time, formatter);
        LocalTime now = LocalTime.now();

        Duration duration = Duration.between(now, userTime);
        long seconds = duration.getSeconds();
        if (seconds < 0) {
            seconds += 24 * 60 * 60;
        }
        return (int) seconds;
    }
}
