import java.util.Map;

public class DarkSky {

    public static Forecast forecast(String key, double latitude, double longitude) {
        return new Forecast(key, latitude, longitude, null, null, Map.of());
    }

    // Overloaded method for time and other parameters
    public static Forecast forecast(String key, double latitude, double longitude, String time, Integer timeout, Map<String, String> queries) {
        return new Forecast(key, latitude, longitude, time, timeout, queries);
    }
}
