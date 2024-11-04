import okhttp3.*;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Resource {

    private static final OkHttpClient client = new OkHttpClient();
    private static final Pattern TYPE_INFO_RE = Pattern.compile("<(\\w+)(<[^>]+>>?)?(?:.+|$)");
    private static final Map<String, Class<?>> PARAM_TYPE_MAP = createParamTypeMap();

    private Phabricator api;
    private Map<String, Object> interfaceData;
    private String endpoint;
    private String method;

    public Resource(Phabricator api, String method, String endpoint, Map<String, Object> interfaceData) {
        this.api = api;
        this.method = method;
        this.endpoint = endpoint;
        this.interfaceData = interfaceData;
    }

    public void setApi(Phabricator api) {
        this.api = api;
    }

    public Map<String, Object> request(Map<String, Object> params) {
        String url = api.getHost() + "/api/" + this.method + "." + this.endpoint;
        params.put("__conduit__", api.getConduit());

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("params", new Gson().toJson(params));
        formBuilder.add("output", "json");

        Request request = new Request.Builder()
            .url(url)
            .post(formBuilder.build())
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = response.body().string();
            return new Gson().fromJson(responseBody, Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Request failed", e);
        }
    }

    public static Map<String, Class<?>> mapParamType(String paramType) {
        Matcher matcher = TYPE_INFO_RE.matcher(paramType);
        if (matcher.find()) {
            String mainType = matcher.group(1);
            return Map.of(paramType, PARAM_TYPE_MAP.getOrDefault(mainType, String.class));
        }
        return Map.of(paramType, String.class);
    }

    public static Map<String, Object> parseInterfaces(Map<String, Object> interfaces) {
        Map<String, Object> parsedInterfaces = new HashMap<>();
        for (Map.Entry<String, Object> entry : interfaces.entrySet()) {
            String method = entry.getKey();
            String[] parts = method.split("\\.");
            Map<String, Object> methodData = (Map<String, Object>) entry.getValue();

            Map<String, Object> appData = (Map<String, Object>) parsedInterfaces.computeIfAbsent(parts[0], k -> new HashMap<>());
            appData.put(parts[1], methodData);
        }
        return parsedInterfaces;
    }

    private static Map<String, Class<?>> createParamTypeMap() {
        Map<String, Class<?>> map = new HashMap<>();
        map.put("int", Integer.class);
        map.put("uint", Integer.class);
        map.put("bool", Boolean.class);
        map.put("map", Map.class);
        map.put("list", java.util.List.class);
        map.put("pair", Map.Entry.class);
        map.put("str", String.class);
        map.put("string", String.class);
        return map;
    }

    public Map<String, Object> getInterfaceData() {
        return interfaceData;
    }

    public void setInterfaceData(Map<String, Object> interfaceData) {
        this.interfaceData = interfaceData;
    }
}
