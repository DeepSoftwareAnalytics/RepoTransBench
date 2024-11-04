import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Configuration {
    private Map<String, Object> json;
    public String url;
    public boolean sslVerify;
    public String bearerToken;
    public String[] sslClient;
    public boolean debug;

    public Configuration(String filename) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        this.json = mapper.readValue(new File(filename), Map.class);
        initialize();
    }

    public Configuration(Map<String, Object> optsDict) {
        this.json = optsDict;
        initialize();
    }

    private void initialize() {
        this.url = getUrl((String) get(new String[]{"url", "ha_url"}));
        this.sslVerify = (boolean) get(new String[]{"ssl_verify", "ha_cert"}, true);
        this.bearerToken = (String) get(new String[]{"bearer_token"}, "");
        this.sslClient = (String[]) get(new String[]{"ssl_client"}, new String[]{});
        this.debug = (boolean) get(new String[]{"debug"}, false);
    }

    public <T> T get(String[] keys, T defaultValue) {
        for (String key : keys) {
            if (json.containsKey(key)) {
                return (T) json.get(key);
            }
        }
        return defaultValue;
    }

    public <T> T get(String[] keys) {
        return get(keys, null);
    }

    public String getUrl(String url) {
        if (url == null) {
            throw new IllegalArgumentException("Property 'url' is missing in config");
        }
        return url.replace("/api", "").replaceAll("/$", "");
    }
}
