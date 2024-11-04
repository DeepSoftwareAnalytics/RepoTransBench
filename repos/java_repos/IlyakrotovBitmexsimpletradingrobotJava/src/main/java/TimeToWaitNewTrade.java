import java.util.HashMap;
import java.util.Map;

public class TimeToWaitNewTrade {
    private static final Map<String, Integer> timeToWaitMap = new HashMap<>();

    static {
        timeToWaitMap.put("1m", 60);
        timeToWaitMap.put("5m", 300);
        timeToWaitMap.put("1h", 3600);
        timeToWaitMap.put("1d", 86400);
    }

    public static int getTimeToWait(String timeframe) {
        return timeToWaitMap.get(timeframe);
    }

    public static Map<String, Integer> getTimeToWaitMap() {
        return timeToWaitMap;
    }
}
