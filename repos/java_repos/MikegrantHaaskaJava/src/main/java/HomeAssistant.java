import com.fasterxml.jackson.databind.ObjectMapper;  // Add this line
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.util.Map;

public class HomeAssistant {
    private Configuration config;
    private CloseableHttpClient httpClient;

    public HomeAssistant(Configuration config) {
        this.config = config;
        this.httpClient = HttpClients.createDefault();
    }

    public String buildUrl(String endpoint) {
        return config.url + "/api/" + endpoint;
    }

    public String getUserAgent() {
        String library = "Home Assistant Alexa Smart Home Skill";
        String awsRegion = System.getenv("AWS_DEFAULT_REGION");
        String defaultUserAgent = "java-httpclient";
        return library + " - " + awsRegion + " - " + defaultUserAgent;
    }

    public Map<String, Object> get(String endpoint) throws IOException {
        HttpGet request = new HttpGet(buildUrl(endpoint));
        request.setHeader("Authorization", "Bearer " + config.bearerToken);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("User-Agent", getUserAgent());

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json = EntityUtils.toString(response.getEntity());
            return new ObjectMapper().readValue(json, Map.class);
        }
    }

    public Map<String, Object> post(String endpoint, Map<String, Object> data, boolean wait) throws IOException {
        HttpPost request = new HttpPost(buildUrl(endpoint));
        request.setHeader("Authorization", "Bearer " + config.bearerToken);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("User-Agent", getUserAgent());

        StringEntity entity = new StringEntity(new ObjectMapper().writeValueAsString(data));
        request.setEntity(entity);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json = EntityUtils.toString(response.getEntity());
            return new ObjectMapper().readValue(json, Map.class);
        }
    }
}
