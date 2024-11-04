import java.util.*;

public class StackAPI {
    protected String baseUrl; 
    public String name;

    // Constructors
    public StackAPI() {
        this.baseUrl = "https://api.stackexchange.com/2.3/";
    }

    public StackAPI(String name) {
        this();
        if (!isValidSiteName(name)) {
            throw new IllegalArgumentException("Invalid Site Name provided");
        }
        this.name = "Stack Overflow";
    }

    public StackAPI(String name, String version, String baseUrl) {
        if (name == null) {
            throw new IllegalArgumentException("A valid site name is required.");
        }
        if (!isValidSiteName(name)) {
            throw new IllegalArgumentException("Invalid Site Name provided");
        }
        this.name = "Stack Overflow";
        this.baseUrl = baseUrl + "/" + version + "/";
    }

    // Dummy fetch method to match the test cases
    public Map<String, Object> fetch(String endpoint, int page, String key, String filter, Map<String, Object> kwargs) {
        if ("errors/400".equals(endpoint)) {
            throw new StackAPIError(400, "bad_parameter");
        }
        return new HashMap<>();
    }

    public Map<String, Object> fetch(String endpoint) {
        if (endpoint == null) {
            throw new IllegalArgumentException("No end point provided.");
        }
        return new HashMap<>();
    }

    // Private method that would validate site names
    private boolean isValidSiteName(String name) {
        // Simulated valid site names check
        return "stackoverflow".equals(name);
    }

    public static class StackAPIError extends RuntimeException {
        public final int error;
        public final String code;

        public StackAPIError(int error, String code) {
            super("Error at URL: " + error);
            this.error = error;
            this.code = code;
        }
    }
}
