import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CommandLineArguments {
    private boolean debug;
    private boolean ssl;
    private boolean insecure;
    private String method = "GET";
    private String host;
    private Integer port;
    private String path;
    private Integer timeout;
    private String auth;
    private String data;
    private Map<String, String> headers;
    private boolean version;
    private int verbose;
    private String separator;
    private String valueSeparator;
    // Add other necessary fields and methods

    public static CommandLineArguments parseArgs(String[] args) {
        CommandLineArguments arguments = new CommandLineArguments();
        // Implement argument parsing logic here, similar to argparse in Python
        return arguments;
    }

    // Getters and setters for all fields
    // Example:
    public boolean isDebug() {
        return debug;
    }

    public boolean isSsl() {
        return ssl;
    }

    public boolean isInsecure() {
        return insecure;
    }

    public String getMethod() {
        return method;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public String getAuth() {
        return auth;
    }

    public String getData() {
        return data;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public boolean isVersion() {
        return version;
    }

    public int getVerbose() {
        return verbose;
    }

    public String getSeparator() {
        return separator;
    }

    public String getValueSeparator() {
        return valueSeparator;
    }
}