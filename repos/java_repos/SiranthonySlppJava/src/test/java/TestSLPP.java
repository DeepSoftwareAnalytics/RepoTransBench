import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class TestSLPP {

    private SLPP slpp = new SLPP();

    @Test
    public void testNumbers() {
        // Integer and float:
        assertEquals(slpp.decode("3"), 3);
        assertEquals(slpp.decode("4.1"), 4.1);
        assertEquals(slpp.encode(3), "3");
        assertEquals(slpp.encode(4.1), "4.1");

        // Negative float:
        assertEquals(slpp.decode("-0.45"), -0.45);
        assertEquals(slpp.encode(-0.45), "-0.45");

        // Scientific:
        assertEquals(slpp.decode("3e-7"), 3e-7);
        assertEquals(slpp.decode("-3.23e+17"), -3.23e+17);
        assertEquals(slpp.encode(3e-7), "3e-07");
        assertEquals(slpp.encode(-3.23e+17), "-3.23e+17");

        // Hex:
        assertEquals(slpp.decode("0x3a"), 0x3a);

        differ(slpp.decode("{\n" +
                "    ID = 0x74fa4cae,\n" +
                "    Version = 0x07c2,\n" +
                "    Manufacturer = 0x21544948\n" +
                "}"), Map.of(
                "ID", 0x74fa4cae,
                "Version", 0x07c2,
                "Manufacturer", 0x21544948
        ));
    }

    @Test
    public void testBool() {
        assertEquals(slpp.encode(true), "true");
        assertEquals(slpp.encode(false), "false");

        assertEquals(slpp.decode("true"), true);
        assertEquals(slpp.decode("false"), false);
    }

    @Test
    public void testNil() {
        assertEquals(slpp.encode(null), "nil");
        assertEquals(slpp.decode("nil"), null);
    }

    @Test
    public void testTable() {
        // Bracketed string key:
        assertEquals(slpp.decode("{[10] = 1}"), Map.of(10, 1));

        // Void table:
        assertEquals(slpp.decode("{nil}"), new HashMap<>());

        // Values-only table:
        assertEquals(slpp.decode("{\"10\"}"), List.of("10"));

        // Last zero
        assertEquals(slpp.decode("{0, 1, 0}"), List.of(0, 1, 0));

        // Mixed encode
        assertEquals(slpp.encode(Map.of("0", 0, "name", "john")), "{\n\t[\"0\"] = 0,\n\t[\"name\"] = \"john\"\n}");
    }

    @Test
    public void testString() {
        // Escape test:
        assertEquals(slpp.decode("'test\\'s string'"), "test's string");

        // Add escaping on encode:
        assertEquals(slpp.encode(Map.of("a", "func(\"call()\");")), "{\n\t[\"a\"] = \"func(\\\"call()\\\");\"\n}");

        // Strings inside double brackets
        String longstr = " (\"word\") . [ [\"word\"] . [\"word\"] . (\"word\" | \"word\" | \"word\" | \"word\") . [\"word\"] ] ";
        assertEquals(slpp.decode("[[" + longstr + "]]"), longstr);
        assertEquals(slpp.decode("{ [0] = [[" + longstr + "]], [1] = \"a\"}"), List.of(longstr, "a"));
    }

    @Test
    public void testBasic() {
        // No data loss:
        String data = "{ array = { 65, 23, 5 }, dict = { string = \"value\", array = { 3, 6, 4}, mixed = { 43, 54.3, false, string = \"value\", 9 } } }";
        Object d = slpp.decode(data);
        differ(d, slpp.decode(slpp.encode(d)));
    }

    @Test
    public void testUnicode() {
        // Note: Unicode tests might need adjustment depending on environment
        assertEquals(slpp.encode("Привет"), "\"Привет\"");
        assertEquals(slpp.encode(Map.of("s", "Привет")), "{\n\t[\"s\"] = \"Привет\"\n}");
    }

    @Test
    public void testConsistency() {
        String[] testCases = {
                "{ 43, 54.3, false, string = \"value\", 9, [4] = 111, [1] = 222, [2.1] = \"text\" }",
                "{ 43, 54.3, false, 9, [5] = 111, [7] = 222 }",
                "{ [7] = 111, [5] = 222, 43, 54.3, false, 9 }",
                "{ 43, 54.3, false, 9, [4] = 111, [5] = 52.1 }",
                "{ [5] = 111, [4] = 52.1, 43, [3] = 54.3, false, 9 }",
                "{ [1] = 1, [2] = \"2\", 3, 4, [5] = 5 }"
        };

        for (String data : testCases) {
            Object d = slpp.decode(data);
            assertEquals(d, slpp.decode(slpp.encode(d)));
        }
    }

    @Test
    public void testComments() {
        String data1 = "-- starting comment\n{\n[\"multiline_string\"] = \"A multiline string where one of the lines starts with\n-- two dashes\",\n-- middle comment\n[\"another_multiline_string\"] = \"A multiline string where one of the lines starts with\n-- two dashes\nfollowed by another line\",\n[\"trailing_comment\"] = \"A string with\" -- a trailing comment\n}\n-- ending comment";
        Map<String, String> res1 = Map.of(
                "multiline_string", "A multiline string where one of the lines starts with\n-- two dashes",
                "another_multiline_string", "A multiline string where one of the lines starts with\n-- two dashes\nfollowed by another line",
                "trailing_comment", "A string with"
        );
        assertEquals(slpp.decode(data1), res1);

        String data2 = "\"--3\"";
        assertEquals(slpp.decode(data2), "--3");

        String data3 = "{\n[\"string\"] = \"A text\n--[[with\ncomment]]\n\",\n--[[\n[\"comented\"] = \"string\nnewline\",\n]]}";
        Map<String, String> res3 = Map.of("string", "A text\n--[[with\ncomment]]\n");
        assertEquals(slpp.decode(data3), res3);
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
}
