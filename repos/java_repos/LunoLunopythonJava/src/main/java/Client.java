import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Client extends BaseClient {

    public Client() {
        super("", 0, "", "");
    }

    public Client(String apiKeyId, String apiKeySecret, String baseUrl, int timeout) {
        super(baseUrl, timeout, apiKeyId, apiKeySecret);
    }

    public Map<String, Object> getTicker(String pair) throws APIError, IOException {
        Map<String, Object> req = new HashMap<>();
        req.put("pair", pair);
        return doRequest("GET", "/api/1/ticker", req, false);
    }
}
