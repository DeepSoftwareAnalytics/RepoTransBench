import java.io.*;
import java.nio.file.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class StandaloneRun {
    private static final Logger logger = Logger.getLogger(StandaloneRun.class.getName());

    public static void main(String[] args) {
        boolean verbose = false;
        boolean pipe = false;
        boolean printResult = false;
        boolean backupOriginal = false;
        int indent = 4;
        Path[] configFiles = new Path[0];

        for (String arg : args) {
            if (arg.equals("-v") || arg.equals("--verbose")) {
                verbose = true;
            } else if (arg.equals("-") || arg.equals("--pipe")) {
                pipe = true;
            } else if (arg.equals("-p") || arg.equals("--print-result")) {
                printResult = true;
            } else if (arg.equals("-b") || arg.equals("--backup-original")) {
                backupOriginal = true;
            } else if (arg.startsWith("-i=") || arg.startsWith("--indent=")) {
                indent = Integer.parseInt(arg.split("=")[1]);
            } else {
                configFiles = Stream.concat(Stream.of(configFiles), Stream.of(Paths.get(arg))).toArray(Path[]::new);
            }
        }

        Logger logger = verbose ? Logger.getLogger(StandaloneRun.class.getName()) : null;
        FormatterOptions formatOptions = new FormatterOptions();
        formatOptions.indentation = indent;
        Formatter formatter = new Formatter(formatOptions, logger);

        try {
            if (pipe) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                StringBuilder inputContents = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    inputContents.append(line).append("\n");
                }
                System.out.print(formatter.formatString(inputContents.toString()));
            } else if (printResult) {
                if (configFiles.length != 1) {
                    throw new IllegalArgumentException("if --print-result is enabled, only one file can be passed as input");
                }
                System.out.print(formatter.getFormattedStringFromFile(configFiles[0]));
            } else {
                for (Path configFilePath : configFiles) {
                    Path backupFilePath = backupOriginal ? Paths.get(configFilePath.toString() + "~") : null;
                    formatter.formatFile(configFilePath, backupFilePath);
                }
            }
        } catch (Exception e) {
            if (logger != null) {
                logger.severe("Error: " + e.getMessage());
            } else {
                e.printStackTrace();
            }
        }
    }
}
