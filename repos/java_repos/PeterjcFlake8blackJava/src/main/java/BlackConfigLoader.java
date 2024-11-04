package main.java;

import com.moandjiezana.toml.Toml;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

public class BlackConfigLoader {

    public static BlackFileMode loadBlackMode() {
        return new BlackFileMode();
    }

    public static BlackFileMode loadBlackMode(Path tomlPath) throws IOException {
        BlackFileMode fileMode = new BlackFileMode();
        Toml toml = new Toml().read(tomlPath.toFile());

        Toml blackConfig = toml.getTable("tool.black");

        if (blackConfig != null) {
            fileMode.lineLength = blackConfig.getLong("line_length", (long) fileMode.lineLength).intValue();
            fileMode.stringNormalization = !blackConfig.getBoolean("skip_string_normalization", !fileMode.stringNormalization);
            fileMode.magicTrailingComma = !blackConfig.getBoolean("skip_magic_trailing_comma", !fileMode.magicTrailingComma);
        }

        return fileMode;
    }

    public static class BlackFileMode {
        public Set<BlackTargetVersion> targetVersions = Collections.emptySet();
        public int lineLength = 88;
        public boolean stringNormalization = true;
        public boolean magicTrailingComma = true;
        public boolean preview = false;
    }

    public enum BlackTargetVersion {
        PYTHON34, PYTHON35, PYTHON36, PYTHON37, PYTHON38, PYTHON39, PYTHON310, PYTHON311
    }
}
