import com.fasterxml.jackson.databind.ObjectMapper; // Ensure this import
import okhttp3.*; // Ensure this import

import java.io.IOException;
import java.util.Map;

public class BaseClient {
    private static final String DEFAULT_BASE_URL = "https://api.luno.com";
    private static final int DEFAULT_TIMEOUT = 10;
    private static OkHttpClient client;
    private ObjectMapper objectMapper;
    private String apiKeyId;
    private String apiKeySecret;
    private String baseUrl;
    private int timeout;

    public BaseClient(String baseUrl, int timeout, String apiKeyId, String apiKeySecret) {
        this.apiKeyId = apiKeyId;
        this.apiKeySecret = apiKeySecret;
        this.baseUrl = baseUrl.isEmpty() ? DEFAULT_BASE_URL : baseUrl;
        this.timeout = timeout == 0 ? DEFAULT_TIMEOUT : timeout;
        this.objectMapper = new ObjectMapper();
        client = new OkHttpClient();
    }

    public String getApiKeyId() {
        return apiKeyId;
    }

    public String getApiKeySecret() {
        return apiKeySecret;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setAuth(String apiKeyId, String apiKeySecret) {
        this.apiKeyId = apiKeyId;
        this.apiKeySecret = apiKeySecret;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl.isEmpty() ? DEFAULT_BASE_URL : baseUrl;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout == 0 ? DEFAULT_TIMEOUT : timeout;
    }

    public Map<String, Object> doRequest(String method, String path, Map<String, Object> req, boolean auth) throws APIError, IOException {
        Request.Builder builder = new Request.Builder().url(makeUrl(path, req));

        if (auth) {
            String credential = Credentials.basic(apiKeyId, apiKeySecret);
            builder.header("Authorization", credential);
        }

        if (method.equals("GET")) {
            builder.get();
        } else if (method.equals("POST")) {
            builder.post(RequestBody.create(objectMapper.writeValueAsString(req), MediaType.get("application/json")));
        }

        Request request = builder.build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = response.body() != null ? response.body().string() : "";
            Map<String, Object> resMap = objectMapper.readValue(responseBody, Map.class);
            if (resMap.containsKey("error") && resMap.containsKey("error_code")) {
                throw new APIError((String) resMap.get("error_code"), (String) resMap.get("error"));
            }
            return resMap;
        } catch (IOException e) {
            throw new IOException("luno: unknown API error on request to " + path, e);
        }
    }

    private String makeUrl(String path, Map<String, Object> params) {
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                path = path.replace("{" + entry.getKey() + "}", entry.getValue().toString());
            }
        }
        return baseUrl + "/" + path;
    }
}

