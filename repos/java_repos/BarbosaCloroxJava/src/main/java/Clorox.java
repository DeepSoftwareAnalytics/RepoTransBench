import java.util.Arrays;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Clorox {

    private static final String[] ALLOWED_FORMATS = {".swift", ".h", ".m"};
    private static final String[] IGNORED_DIRS = {
            ".xcdatamodel", ".xcdatamodeld",
            ".xcassets", ".imageset",
            ".bundle", ".framework", ".lproj"
    };

    private final Arguments args;
    private final Printer printer;
    private final List<String> allFiles;
    private final List<String> modifiedFiles;

    public Clorox(Arguments args) {
        this.args = args;
        this.printer = new Printer(args);
        this.allFiles = new ArrayList<>();
        this.modifiedFiles = new ArrayList<>();
    }

    public void run() {
        printer.printStart();

        for (String path : args.getPath()) {
            File file = new File(path);
            if (file.isFile()) {
                processFile(path);
            } else if (file.isDirectory()) {
                processDir(path);
            }
        }

        printer.printEnd(allFiles.toArray(new String[0]), modifiedFiles.toArray(new String[0]));
    }

    private void processDir(String dirPath) {
        File dir = new File(dirPath);
        printer.printDir(dirPath);

        for (File file : dir.listFiles()) {
            if (file.isDirectory() && !shouldIgnoreDir(file.getName())) {
                processDir(file.getAbsolutePath());
            } else if (isAllowedFormat(file.getName())) {
                processFile(file.getAbsolutePath());
            }
        }
    }

    private void processFile(String filePath) {
        allFiles.add(filePath);
        boolean hasHeader = findXcodeHeader(filePath);

        if (hasHeader) {
            boolean succeeded = true;
            if (!args.isInspection()) {
                String updatedContent = getUpdatedContent(filePath);
                succeeded = removeHeader(filePath, updatedContent);
            }

            modifiedFiles.add(filePath);
            printer.printFile(filePath, succeeded);
        }
    }

    private boolean findXcodeHeader(String filePath) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            HeaderMatcher matcher = new HeaderMatcher(content, args.isTrim());
            String header = matcher.match();
            return header != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getUpdatedContent(String filePath) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            HeaderMatcher matcher = new HeaderMatcher(content, args.isTrim());
            String header = matcher.match();
            return content.replace(header, "");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean removeHeader(String filePath, String updatedContent) {
        try {
            Files.write(Paths.get(filePath), updatedContent.getBytes());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isAllowedFormat(String fileName) {
        for (String format : ALLOWED_FORMATS) {
            if (fileName.endsWith(format)) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldIgnoreDir(String dirName) {
        for (String ignoredDir : IGNORED_DIRS) {
            if (dirName.endsWith(ignoredDir)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        Arguments arguments = new Arguments(
                args, // Path argument is passed as it is
                true, // Default value for trim argument
                false, // Default value for inspection argument
                false, // Default value for quiet argument
                null // Default reporter is null
        );

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-t":
                case "--trim":
                    arguments.setTrim(true);
                    break;
                case "-i":
                case "--inspection":
                    arguments.setInspection(true);
                    break;
                case "-q":
                case "--quiet":
                    arguments.setQuiet(true);
                    break;
                case "-r":
                case "--reporter":
                    // Assume the reporter argument comes with a value
                    if (i + 1 < args.length) {
                        arguments.setReporter(args[i + 1]);
                        i++; // Skip the next argument which is the value for --reporter
                    }
                    break;
                default:
                    // Unknown arguments will be treated as paths
                    break;
            }
        }

        new Clorox(arguments).run();
    }
}
