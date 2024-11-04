import java.util.List;
import java.util.Map;

public class DataPoint {
    protected Map<String, Object> data;

    public DataPoint(Map<String, Object> data) {
        this.data = data;

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            this.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    public void setAttribute(String name, Object value) {
        if (value instanceof Map) {
            value = new DataPoint((Map<String, Object>) value);
        }

        this.data.put(name, value);
    }

    public Object getAttribute(String key) {
        return this.data.get(key);
    }

    public int getSize() {
        return this.data.size();
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
