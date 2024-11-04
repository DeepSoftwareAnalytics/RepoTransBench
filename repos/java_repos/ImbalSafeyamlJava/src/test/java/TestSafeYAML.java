import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import org.yaml.snakeyaml.Yaml;

import static org.junit.jupiter.api.Assertions.*;

class TestSafeYAML {
    private static final Map<String, Object> SMOKE_TESTS = new HashMap<>();
    static {
        SMOKE_TESTS.put(" [0] ", Collections.singletonList(0));
        SMOKE_TESTS.put(" [1.2] ", Collections.singletonList(1.2));
        SMOKE_TESTS.put(" [-3.4] ", Collections.singletonList(-3.4));
        SMOKE_TESTS.put(" [+5.6] ", Collections.singletonList(+5.6));
        SMOKE_TESTS.put(" \"test\": 1 ", Map.of("test", 1));
        SMOKE_TESTS.put(" x: 'test' ", Map.of("x", "test"));
        SMOKE_TESTS.put(" [1 ,2,3] ", Arrays.asList(1, 2, 3));
        SMOKE_TESTS.put(" [1,2,3,] ", Arrays.asList(1, 2, 3));
        SMOKE_TESTS.put(" {\"a\":1} ", Map.of("a", 1));
        SMOKE_TESTS.put(" {'b':2,} ", Map.of("b", 2));
        SMOKE_TESTS.put(" [1  #foo\n] ", Collections.singletonList(1));
    }

    @ParameterizedTest
    @MethodSource("smokeTestProvider")
    void testSmoke(String code, Object refObj) throws ParserError {
        SafeYAML safeYAML = new SafeYAML();
        StringWriter output = new StringWriter();
        Options options = new Options(false, false, false, false);
        List<Object> objs = safeYAML.parse(code, output, options);
        assertEquals(refObj, objs.get(0));
    }

    static Stream<Arguments> smokeTestProvider() {
        return SMOKE_TESTS.entrySet().stream().map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
    }

    @Test
    void testValidate() throws IOException {
        for (String path : Files.readAllLines(Path.of("tests/validate"))) {
            checkFile(path, true, false);
        }
    }

    @Test
    void testFix() throws IOException {
        for (String path : Files.readAllLines(Path.of("tests/fix"))) {
            checkFile(path, false, true);
        }
    }

    private void checkFile(String path, boolean validate, boolean fix) throws IOException {
        SafeYAML safeYAML = new SafeYAML();
        String contents = new String(Files.readAllBytes(Paths.get(path)));
        Options options = new Options(fix, fix, false, false);
        StringWriter output = new StringWriter();
        List<Object> objs;

        try {
            objs = safeYAML.parse(contents, output, options);
        } catch (ParserError e) {
            String[] expectedError = Files.readAllLines(Paths.get(path + ".error")).get(0).split(":");
            String errorName = expectedError[0];
            int errorPos = Integer.parseInt(expectedError[1]);
            assertEquals(errorName, e.name());
            assertEquals(errorPos, e.getPos());
            return;
        }

        String yamlOutput = output.toString();

        if (validate) {
            Yaml yaml = new Yaml();
            Object refObj = yaml.load(contents);

            assertEquals(refObj, objs.get(0));

            Object parsedOutput = yaml.load(yamlOutput);
            assertEquals(refObj, parsedOutput);
        }

        if (fix) {
            String expectedOutput = new String(Files.readAllBytes(Paths.get(path + ".output")));
            assertEquals(expectedOutput, yamlOutput);
        }
    }
}
