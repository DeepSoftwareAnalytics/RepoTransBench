import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class SteamAPI {

    protected String apiKey;
    protected String steamId;
    protected int time;
    protected int retries;
    protected JSONObject rawJson;

    public SteamAPI(String steamId, String apiKey) {
        this.apiKey = apiKey;
        this.steamId = steamId;
        this.time = 10;
        this.retries = 3;
    }

    protected JSONObject getJson(String url, String... params) {
        if (params.length > 0) {
            url = String.format(url, (Object[]) params);
        }
        return new JSONObject(getResponse(url));
    }

    private String getResponse(String url) {
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder content = new StringBuilder();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            return content.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Can't connect to Steam. Try again later.");
        }
    }

    protected LocalDateTime convertToLocalDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
    }
}
