import java.util.Map;

public class Result {
    private final Map<String, Object> response;

    public Result(Map<String, Object> response) {
        this.response = response;
    }

    public Object get(String key) {
        return response.get(key);
    }

    public void set(String key, Object value) {
        response.put(key, value);
    }

    public int size() {
        return response.size();
    }

    @Override
    public String toString() {
        return "Result{" +
                "response=" + response +
                '}';
    }
}
