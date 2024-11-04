import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class TestUtilityFunctions {

    private boolean isIterator(Object obj) {
        try {
            Iterator<?> it = ((Iterable<?>) obj).iterator();
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }

    private void differ(Object value, Object origin) {
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
            assertEquals(value, origin, value + " does not match original: " + origin);
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

        assertEquals(value, origin, value + " does not match original: " + origin);
    }

    @Test
    public void testIsIterator() {
        assertTrue(isIterator(new ArrayList<>()));
        assertFalse(isIterator(1));
    }

    @Test
    public void testDiffer() {
        // Same:
        differ(1, 1);
        differ(Arrays.asList(2, 3), Arrays.asList(2, 3));
        differ(Map.of("1", 3, "4", "6"), Map.of("4", "6", "1", 3));
        differ("4", "4");

        // Different:
        assertThrows(AssertionError.class, () -> differ(1, 2));
        assertThrows(AssertionError.class, () -> differ(Arrays.asList(2, 3), Arrays.asList(3, 2)));
        assertThrows(AssertionError.class, () -> differ(Map.of("6", 4, "3", "1"), Map.of("4", "6", "1", 3)));
        assertThrows(AssertionError.class, () -> differ("4", "no"));
    }
}
