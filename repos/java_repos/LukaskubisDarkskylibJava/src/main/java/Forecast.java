import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.fluent.Request;  // Ensure this import statement is here
import java.io.IOException;
import java.util.Map;

public class Forecast extends DataPoint {
    
    private Map<String, Object> parameters;
    private String key;
    
    public Forecast(String key, double latitude, double longitude, String time, Integer timeout, Map<String, String> queries) {
        super(Map.of());
        this.key = key;
        this.parameters = Map.of(
            "key", key,
            "latitude", latitude,
            "longitude", longitude,
            "time", time
        );
        
        refresh(timeout, queries);
    }

    public String getUrl() {
        String timeString = parameters.get("time") != null ? "," + parameters.get("time") : "";
        return String.format("https://api.darksky.net/forecast/%s/%f,%f%s", key, parameters.get("latitude"), parameters.get("longitude"), timeString);
    }

    public void refresh(Integer timeout, Map<String, String> queries) {
        try {
            Request request = Request.Get(getUrl())
                                     .addHeader("Accept-Encoding", "gzip");
            if (timeout != null) {
                request.socketTimeout(timeout);
            }

            String responseString = request.execute().returnContent().asString();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> data = mapper.readValue(responseString, Map.class);

            // Calling superclass constructor with the response data
            super.setData(data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve data", e);
        }
    }
}
