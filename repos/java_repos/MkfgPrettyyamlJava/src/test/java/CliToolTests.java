import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class CliToolTests {

    enum TestConst {
        DISPATCH(2455),
        HEARTBEAT(123);

        private final int value;

        TestConst(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private static Map<String, Object> data = new HashMap<>();

    static {
        Map<String, Object> queryDump = new HashMap<>();
        queryDump.put("key1", "тест1");
        queryDump.put("key2", "тест2");
        queryDump.put("key3", "тест3");
        queryDump.put("последний", null);

        Map<String, Object> ids = new HashMap<>();
        ids.put("id в уникоде", Arrays.asList(4, 5, 6));
        ids.put("id2 в уникоде", Arrays.asList(4, 5, 6));

        data.put("key", "value");
        data.put("path", "/some/path");
        data.put("query_dump", queryDump);
        data.put("query_dump_clone", queryDump);
        data.put("ids", ids);
        data.put("a", Arrays.asList(1, null, "asd", "не-ascii"));
        data.put("b", 3.5);
        data.put("c", null);
        data.put("'asd'\n!\0\1", Map.of("b", 1, "a", 2));
    }

    private String dataHash(Map<String, Object> data) throws IOException {
        return new String(Files.readAllBytes(Paths.get("data.json")));
    }

    @Test
    public void testSuccess() throws Exception {
        Map<String, Object> d = new HashMap<>(data);
        StringWriter out = new StringWriter();
        StringWriter err = new StringWriter();

        Yaml yaml = new Yaml();
        String ys = yaml.dump(d);

        CliTool.main(new String[]{}, new StringReader(ys), out, err);
        yaml.load(out.toString());

        assertTrue(out.toString().length() > 150);
        assertEquals("", err.toString());

        d.put("d", TestConst.HEARTBEAT.getValue());
        d.put("asd", new LinkedHashMap<>(Map.of("b", 1, "a", 2)));

        ys = new Yaml().dump(d);
        out = new StringWriter();
        CliTool.main(new String[]{}, new StringReader(ys), out, err);
        yaml.load(out.toString());

        assertTrue(out.toString().length() > 150);
        assertEquals("", err.toString());
    }

    @Test
    public void testVSpacingFlags() throws Exception {
        Map<String, Object> d = new HashMap<>(data);
        StringWriter out = new StringWriter();
        StringWriter err = new StringWriter();

        Yaml yaml = new Yaml();
        String ys = yaml.dump(d);

        Set<String> outs = new HashSet<>();
        String[] ins = {"", "-v0/0", "-v0/0s", "-v0/0sg", "-vg"};

        for (String argv : ins) {
            out = new StringWriter();
            CliTool.main(argv.split(" "), new StringReader(ys), out, err);
            assertFalse(outs.contains(out.toString()));
            assertTrue(out.toString().length() > 150);
            assertEquals("", err.toString());
            outs.add(out.toString());
        }

        assertEquals(ins.length, outs.size());
    }

    @Test
    public void testLoadFail() {
        Map<String, Object> d = new HashMap<>(data);
        StringWriter out = new StringWriter();
        StringWriter err = new StringWriter();

        Yaml yaml = new Yaml();
        String ys = yaml.dump(d) + "\0asd : fgh : ghj\0";

        assertThrows(YAMLException.class, () -> {
            CliTool.main(new String[]{}, new StringReader(ys), out, err);
        });
    }

    @Test
    public void testOutBroken() {
        Map<String, Object> d = new HashMap<>(data);
        StringWriter out = new StringWriter();
        StringWriter err = new StringWriter();

        Yaml yaml = new Yaml();
        String ys = yaml.dump(d);

        assertThrows(YAMLException.class, () -> {
            CliTool.main(new String[]{}, new StringReader(ys), out, err);
            yaml.load(out.toString());
        });

        assertTrue(out.toString().length() > 150);
        assertTrue(err.toString().matches("^WARNING:"));
    }

    @Test
    public void testSingleDoc() throws Exception {
        Map<String, Object> d = new HashMap<>(data);
        StringWriter out = new StringWriter();
        StringWriter err = new StringWriter();

        Yaml yaml = new Yaml();
        String ys = yaml.dump(d);

        CliTool.main(new String[]{}, new StringReader(ys), out, err);
        assertFalse(out.toString().contains("---"));

        CliTool.main(new String[]{}, new StringReader("---\n" + ys), out, err);
        assertFalse(out.toString().contains("---"));
        assertEquals(1, yaml.loadAll(out.toString()).iterator().next().size());
    }

    @Test
    public void testMultiDoc() throws Exception {
        Map<String, Object> d = new HashMap<>(data);
        Map<String, Object> d2 = new HashMap<>(d);
        d2.put("doc2_val", 1234);

        Yaml yaml = new Yaml();
        String ys = yaml.dumpAll(Arrays.asList(d, d2));

        StringWriter out = new StringWriter();
        StringWriter err = new StringWriter();

        CliTool.main(new String[]{}, new StringReader(ys), out, err);

        assertTrue(out.toString().length() > 150);
        assertEquals("", err.toString());
        assertTrue(out.toString().contains("---"));

        Iterator<Object> loadedDocs = yaml.loadAll(out.toString()).iterator();
        Map<String, Object> xd1 = (Map<String, Object>) loadedDocs.next();
        Map<String, Object> xd2 = (Map<String, Object>) loadedDocs.next();

        assertFalse(xd1.containsKey("doc2_val"));
        assertEquals(1234, xd2.get("doc2_val"));
    }

    // More tests following the same pattern...

    public static void main(String[] args) throws Exception {
        org.junit.runner.JUnitCore.main("pyaml.cli.CliToolTests");
    }
}
