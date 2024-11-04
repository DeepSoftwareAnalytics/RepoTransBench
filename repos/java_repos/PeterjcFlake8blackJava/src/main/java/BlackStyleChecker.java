package main.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.NoSuchFileException;

public class BlackStyleChecker {

    public Path filename;
    public BlackConfigLoader.BlackFileMode overrideConfig = null;

    public BlackStyleChecker(Path filename) {
        this.filename = filename;
    }

    public void run() throws IOException {
        String source = Files.readString(filename);
        if (source.isEmpty()) {
            // Empty file, pass
            return;
        }
        BlackConfigLoader.BlackFileMode fileMode = overrideConfig != null ? overrideConfig : loadBlackModeWithFallback(filename.getParent().resolve("pyproject.toml"));
        // Pseudo code for formatting
        // String newCode = Black.formatFileContents(source, fileMode);
        // if (!newCode.equals(source)) {
        //     int[] diff = FindDiff.findDiffStart(source, newCode);
        //     // raise appropriate message
        // }
    }
    
    private BlackConfigLoader.BlackFileMode loadBlackModeWithFallback(Path tomlPath) throws IOException {
        try {
            return BlackConfigLoader.loadBlackMode(tomlPath);
        } catch (NoSuchFileException e) {
            return BlackConfigLoader.loadBlackMode();
        }
    }
}
