import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class TestError {
    @Test
    public void testErrorStr() {
        Error error = new Error("Test error");
        assertEquals("Test error", error.toString());
    }

    @Test
    public void testInvalidPath() {
        Invalid invalid = new Invalid("Invalid error", List.of("a", "b"));
        assertEquals(List.of("a", "b"), invalid.getPath());
    }

    @Test
    public void testInvalidMsg() {
        Invalid invalid = new Invalid("Invalid error");
        assertEquals("Invalid error", invalid.getMessage());
    }

    @Test
    public void testMultipleInvalid() {
        List<Invalid> errors = List.of(new Invalid("Error 1"), new Invalid("Error 2"));
        MultipleInvalid multipleInvalid = new MultipleInvalid(errors);
        assertEquals(2, multipleInvalid.getErrors().size());
    }

    @Test
    public void testSchemaError() {
        SchemaError schemaError = new SchemaError("Schema error");
        assertEquals("Schema error", schemaError.toString());
    }
}
