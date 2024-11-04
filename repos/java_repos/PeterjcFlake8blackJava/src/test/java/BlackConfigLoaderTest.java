package test.java;

import main.java.BlackConfigLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlackConfigLoaderTest {
    
    @Test
    public void testDefaultLoad() {
        BlackConfigLoader.BlackFileMode mode = BlackConfigLoader.loadBlackMode();
        assertEquals(88, mode.lineLength);
    }

    @Test
    public void testLoadValidToml(@TempDir Path tempDir) throws IOException {
        Path tomlPath = tempDir.resolve("pyproject.toml");
        Files.writeString(tomlPath, "[tool.black]\nline_length = 100\n");
        BlackConfigLoader.BlackFileMode mode = BlackConfigLoader.loadBlackMode(tomlPath);
        assertEquals(100, mode.lineLength);
    }

    @Test
    public void testLoadTomlWithStringNormalization(@TempDir Path tempDir) throws IOException {
        Path tomlPath = tempDir.resolve("pyproject.toml");
        Files.writeString(tomlPath, "[tool.black]\nskip_string_normalization = true\n");
        BlackConfigLoader.BlackFileMode mode = BlackConfigLoader.loadBlackMode(tomlPath);
        assertFalse(mode.stringNormalization);
    }

    @Test
    public void testLoadTomlWithTrailingComma(@TempDir Path tempDir) throws IOException {
        Path tomlPath = tempDir.resolve("pyproject.toml");
        Files.writeString(tomlPath, "[tool.black]\nskip_magic_trailing_comma = true\n");
        BlackConfigLoader.BlackFileMode mode = BlackConfigLoader.loadBlackMode(tomlPath);
        assertFalse(mode.magicTrailingComma);
    }
}
