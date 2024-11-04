import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.python.util.PythonInterpreter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class SelfTestCase {

    private PythonInterpreter interpreter;

    @BeforeEach
    public void setUp() {
        interpreter = new PythonInterpreter();
        interpreter.exec("import gast");
    }

    @Test
    public void testParse() throws IOException {
        List<String> srcs = Files.walk(Paths.get("src/main/java/"))
                .filter(Files::isRegularFile)
                .map(Path::toString)
                .collect(Collectors.toList());

        for (String src : srcs) {
            String content = new String(Files.readAllBytes(Paths.get(src)));
            assertDoesNotThrow(() -> interpreter.exec("gast.parse(\"\"\"" + content + "\"\"\")"));
        }
    }

    @Test
    public void testCompile() throws IOException {
        List<String> srcs = Files.walk(Paths.get("src/main/java/"))
                .filter(Files::isRegularFile)
                .map(Path::toString)
                .collect(Collectors.toList());

        for (String src : srcs) {
            String content = new String(Files.readAllBytes(Paths.get(src)));
            interpreter.exec("gnode = gast.parse(\"\"\"" + content + "\"\"\")");
            assertDoesNotThrow(() -> interpreter.exec("compile(gast.gast_to_ast(gnode), \"" + src + "\", 'exec')"));
        }
    }

    @Test
    public void testUnparse() throws IOException {
        List<String> srcs = Files.walk(Paths.get("src/main/java/"))
                .filter(Files::isRegularFile)
                .map(Path::toString)
                .collect(Collectors.toList());

        for (String src : srcs) {
            String content = new String(Files.readAllBytes(Paths.get(src)));
            interpreter.exec("gnode = gast.parse(\"\"\"" + content + "\"\"\")");
            assertDoesNotThrow(() -> interpreter.exec("gast.unparse(gnode)"));
        }
    }
}
