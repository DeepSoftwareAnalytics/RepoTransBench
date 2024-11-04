package test.java;

import main.java.BlackStyleChecker;
import main.java.BlackConfigLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class BlackStyleCheckerTest {

    private BlackStyleChecker checker;

    @BeforeEach
    public void setUp(@TempDir Path tempDir) {
        checker = new BlackStyleChecker(tempDir.resolve("test.py"));
    }

    @Test
    public void testCheckerInitialization() {
        assertEquals("test.py", checker.filename.getFileName().toString());
    }

    @Test
    public void testRunOnEmptyFile(@TempDir Path tempDir) throws IOException {
        Path emptyFile = tempDir.resolve("empty.py");
        Files.writeString(emptyFile, "");
        checker.filename = emptyFile;
        checker.run();
        // Placeholder for further checks or assertions
    }

    @Test
    public void testRunOnValidCode(@TempDir Path tempDir) throws IOException {
        Path tempFile = Files.createTempFile(tempDir, "test", ".py");
        Files.writeString(tempFile, "print('hello world')\n");

        // Creating a dummy pyproject.toml to avoid the FileNotFoundException
        Path tomlFile = tempDir.resolve("pyproject.toml");
        Files.writeString(tomlFile, "[tool.black]\nline_length = 88\n");

        checker.filename = tempFile;
        checker.run();
        Files.deleteIfExists(tempFile);
        // Placeholder for further checks or assertions
    }

    @Test
    public void testCheckerWithOverrideConfig() {
        checker.overrideConfig = new BlackConfigLoader.BlackFileMode();
        checker.overrideConfig.lineLength = 80;
        assertEquals(80, checker.overrideConfig.lineLength);
    }

    @Test
    public void testCheckerWithCustomConfig() {
        checker.overrideConfig = new BlackConfigLoader.BlackFileMode();
        checker.overrideConfig.stringNormalization = false;
        assertFalse(checker.overrideConfig.stringNormalization);
    }
}
