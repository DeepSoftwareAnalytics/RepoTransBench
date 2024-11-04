import java.util.Map;
import java.util.HashMap;
import java.util.List;  // Add this import
import java.util.ArrayList;  // Add this import

public class Schema {
    private Map<String, Class<?>> schema;
    private int extra;

    public static final int PREVENT_EXTRA = 0;
    public static final int ALLOW_EXTRA = 1;
    public static final int REMOVE_EXTRA = 2;

    public Schema(Map<String, Class<?>> schema, int extra) {
        this.schema = schema;
        this.extra = extra;
    }

    public Schema(Map<String, Class<?>> schema) {
        this(schema, PREVENT_EXTRA);
    }

    public Map<String, Object> validate(Map<String, Object> data) throws MultipleInvalid {
        Map<String, Object> result = new HashMap<>();
        List<Invalid> errors = new ArrayList<>();  // Added and fixed to use ArrayList

        for (Map.Entry<String, Class<?>> entry : schema.entrySet()) {
            String key = entry.getKey();
            Class<?> type = entry.getValue();

            if (!data.containsKey(key)) {
                errors.add(new Invalid("required key not provided", List.of(key)));
                continue;
            }

            Object value = data.get(key);
            if (!type.isInstance(value)) {
                errors.add(new Invalid("expected " + type.getSimpleName(), List.of(key)));
                continue;
            }

            result.put(key, value);
        }

        if (extra == PREVENT_EXTRA) {
            for (String key : data.keySet()) {
                if (!schema.containsKey(key)) {
                    errors.add(new Invalid("extra keys not allowed", List.of(key)));
                }
            }
        } else if (extra == REMOVE_EXTRA) {
            for (String key : data.keySet()) {
                if (!schema.containsKey(key)) {
                    data.remove(key);
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new MultipleInvalid(errors);
        }

        return result;
    }
}
