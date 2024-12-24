import java.util.*;

public class UtilityFunctions {

    public static boolean isIterator(Object obj) {
        try {
            Iterator<?> it = ((Iterable<?>) obj).iterator();
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public static void differ(Object value, Object origin) {
        if (!value.getClass().equals(origin.getClass())) {
            throw new AssertionError("Types do not match: " + value.getClass() + ", " + origin.getClass());
        }

        if (origin instanceof Map) {
            Map<?, ?> originMap = (Map<?, ?>) origin;
            Map<?, ?> valueMap = (Map<?, ?>) value;
            for (Map.Entry<?, ?> entry : originMap.entrySet()) {
                Object key = entry.getKey();
                Object item = entry.getValue();
                if (!valueMap.containsKey(key)) {
                    throw new AssertionError(value + " does not match original: " + origin + "; Key: " + key + ", item: " + item);
                }
                differ(valueMap.get(key), item);
            }
            return;
        }

        if (origin instanceof String) {
            if (!value.equals(origin)) {
                throw new AssertionError(value + " does not match original: " + origin);
            }
            return;
        }

        if (origin instanceof Iterable) {
            List<?> originList = (List<?>) origin;
            List<?> valueList = (List<?>) value;
            for (int i = 0; i < originList.size(); i++) {
                if (i >= valueList.size()) {
                    throw new AssertionError(value + " does not match original: " + origin + ". Item " + originList.get(i) + " not found");
                }
                differ(valueList.get(i), originList.get(i));
            }
            return;
        }

        if (!value.equals(origin)) {
            throw new AssertionError(value + " does not match original: " + origin);
        }
    }
}
