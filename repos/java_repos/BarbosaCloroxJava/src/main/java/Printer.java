import java.io.File;
import java.util.Map;

public class Printer {

    private final Reporter reporter;
    private final Arguments args;

    public Printer(Arguments args) {
        this.args = args;
        this.reporter = Reporter.fromIdentifier(args.getReporter());
    }

    public void printStart() {
        print("Running...");
    }

    public void printDir(String path) {
        String dirname = new File(path).getName();
        String tabs = tabs(path);
        print(Color.PURPLE + tabs + dirname + "/" + Color.END);
    }

    public void printFile(String path, boolean succeeded) {
        String fileName = new File(path).getName();
        String color = succeeded ? Color.END : Color.RED;
        String labelColor = args.isInspection() ? Color.YELLOW : Color.GREEN;
        String feedback = args.isInspection() ? "(would be modified)" : "(done)";

        print(tabs(path) + colored(fileName, color) + " " + colored(feedback, labelColor));
    }

    public void printEnd(String[] allFiles, String[] modifiedFiles) {
        if (reporter != null) {
            reporter.report(allFiles, modifiedFiles);
        } else {
            print("\nTotal files: " + allFiles.length);
            if (args.isInspection()) {
                print("Files it would modify: " + modifiedFiles.length);
            } else {
                print("Modified files: " + modifiedFiles.length);
            }
        }
    }

    private String tabs(String path) {
        return " ".repeat(2 * path.split(File.separator).length);
    }

    private void print(String message) {
        if (!args.isQuiet() && reporter == null) {
            System.out.println(message);
        }
    }

    private String colored(String string, String color) {
        return color + string + Color.END;
    }

    public static class Color {
        public static final String PURPLE = "\033[95m";
        public static final String BLUE = "\033[94m";
        public static final String GREEN = "\033[92m";
        public static final String YELLOW = "\033[93m";
        public static final String RED = "\033[91m";
        public static final String END = "\033[0m";
        public static final String BOLD = "\033[1m";
        public static final String UNDERLINE = "\033[4m";
    }
}
