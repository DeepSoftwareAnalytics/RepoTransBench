import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class CoverageBadgeMain {

    public static final String VERSION = "1.1.1";
    public static final String DEFAULT_COLOR = "#a4a61d";

    public static final Map<String, String> COLORS;
    public static final int[][] COLOR_RANGES = {
        {95, 0x4c1}, // brightgreen
        {90, 0x97CA00}, // green
        {75, 0xa4a61d}, // yellowgreen
        {60, 0xdfb317}, // yellow
        {40, 0xfe7d37}, // orange
        {0, 0xe05d44} // red
    };

    static {
        COLORS = new HashMap<>();
        COLORS.put("brightgreen", "#4c1");
        COLORS.put("green", "#97CA00");
        COLORS.put("yellowgreen", "#a4a61d");
        COLORS.put("yellow", "#dfb317");
        COLORS.put("orange", "#fe7d37");
        COLORS.put("red", "#e05d44");
        COLORS.put("lightgrey", "#9f9f9f");
    }

    public static void main(String[] args) {
        Args parsedArgs = parseArgs(args);

        // Print version
        if (parsedArgs.printVersion) {
            System.out.println("coverage-badge v" + VERSION);
            return;
        }

        // Generate badge
        String total = get_total();
        String color = parsedArgs.plainColor ? DEFAULT_COLOR : getColor(total);
        String badge = getBadge(total, color);

        // Show or save output
        if (parsedArgs.filepath != null) {
            try {
                saveBadge(badge, parsedArgs.filepath, parsedArgs.force);
                if (!parsedArgs.quiet) {
                    System.out.println("Saved badge to " + parsedArgs.filepath);
                }
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
        } else {
            System.out.print(badge);
        }
    }

    public static String get_total() {
        // Mocking coverage percentage for this implementation
        return "79";
    }

    public static String getColor(String total) {
        int coverage;
        try {
            coverage = Integer.parseInt(total);
        } catch (NumberFormatException e) {
            return COLORS.get("lightgrey");
        }

        for (int[] range : COLOR_RANGES) {
            if (coverage >= range[0]) {
                return String.format("#%06X", range[1]);
            }
        }

        return DEFAULT_COLOR;
    }

    public static String getBadge(String total, String color) {
        try {
            String template = new String(Files.readAllBytes(Paths.get("templates/flat.svg")), "UTF-8");
            return template.replace("{{ total }}", total).replace("{{ color }}", color);
        } catch (IOException e) {
            throw new RuntimeException("Error reading SVG template", e);
        }
    }

    public static void saveBadge(String badge, String filepath, boolean force) throws IOException {
        File file = new File(filepath);
        if (file.exists() && !force) {
            throw new IOException("\"" + filepath + "\" already exists.");
        }

        Files.write(Paths.get(filepath), badge.getBytes());
    }

    public static Args parseArgs(String[] args) {
        Args parsedArgs = new Args();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-o":
                    if (i + 1 < args.length) {
                        parsedArgs.filepath = args[++i];
                    } else {
                        throw new IllegalArgumentException("-o requires a file path");
                    }
                    break;
                case "-p":
                    parsedArgs.plainColor = true;
                    break;
                case "-f":
                    parsedArgs.force = true;
                    break;
                case "-q":
                    parsedArgs.quiet = true;
                    break;
                case "-v":
                    parsedArgs.printVersion = true;
                    break;
            }
        }

        return parsedArgs;
    }

    public static class Args {
        String filepath;
        boolean plainColor = false;
        boolean force = false;
        boolean quiet = false;
        boolean printVersion = false;
    }
}
