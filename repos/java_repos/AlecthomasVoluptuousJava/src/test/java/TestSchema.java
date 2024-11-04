import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.HashMap;

public class TestSchema {
    @Test
    public void testSchemaValid() throws MultipleInvalid {
        Map<String, Class<?>> schemaMap = new HashMap<>();
        schemaMap.put("a", Integer.class);
        Schema schema = new Schema(schemaMap);
        Map<String, Object> data = new HashMap<>();
        data.put("a", 1);
        assertEquals(data, schema.validate(data));
    }

    @Test
    public void testSchemaInvalid() {
        Map<String, Class<?>> schemaMap = new HashMap<>();
        schemaMap.put("a", Integer.class);
        Schema schema = new Schema(schemaMap);
        Map<String, Object> data = new HashMap<>();
        data.put("a", "1");
        assertThrows(MultipleInvalid.class, () -> schema.validate(data));
    }

    @Test
    public void testSchemaExtra() throws MultipleInvalid {
        Map<String, Class<?>> schemaMap = new HashMap<>();
        schemaMap.put("a", Integer.class);
        Schema schema = new Schema(schemaMap, Schema.ALLOW_EXTRA);
        Map<String, Object> data = new HashMap<>();
        data.put("a", 1);
        data.put("b", 2);
        assertEquals(data, schema.validate(data));
    }

    @Test
    public void testSchemaRemoveExtra() throws MultipleInvalid {
        Map<String, Class<?>> schemaMap = new HashMap<>();
        schemaMap.put("a", Integer.class);
        Schema schema = new Schema(schemaMap, Schema.REMOVE_EXTRA);
        Map<String, Object> data = new HashMap<>();
        data.put("a", 1);
        data.put("b", 2);
        Map<String, Object> expected = new HashMap<>();
        expected.put("a", 1);
        assertEquals(expected, schema.validate(data));
    }
}
