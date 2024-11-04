import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class DingArgs {
    public boolean noTimer = false;
    public String command = null;
    public String mode;
    public String[] time;

    private static final Pattern RELATIVE_TIME_PATTERN = Pattern.compile("\\d+[smh]( +\\d+[smh])*");
    private static final Pattern ABSOLUTE_TIME_PATTERN = Pattern.compile("\\d{1,2}(:\\d{1,2}(:\\d{1,2})?)?");

    public void parseArgs(String[] args) {
        int index = 0;
        while (index < args.length) {
            String arg = args[index];
            switch (arg) {
                case "-n":
                case "--no-timer":
                    noTimer = true;
                    break;
                case "-c":
                case "--command":
                    command = args[++index];
                    break;
                case "in":
                case "every":
                    mode = arg;
                    time = java.util.Arrays.copyOfRange(args, index + 1, args.length);
                    validateRelativeTime();
                    return;
                case "at":
                    mode = arg;
                    time = new String[]{args[index + 1]};
                    validateAbsoluteTime();
                    return;
                default:
                    throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            index++;
        }
        throw new IllegalArgumentException("Argument mode not specified.");
    }

    private void validateRelativeTime() {
        for (String t : time) {
            validateRelativeTime(t);  // Call the static method to validate each time string
        }
    }

    private void validateAbsoluteTime() {
        validateAbsoluteTime(time[0]);  // Call the static method to validate the absolute time string
    }

    public static void validateRelativeTime(String time) {
        if (!RELATIVE_TIME_PATTERN.matcher(time).matches()) {
            throw new IllegalArgumentException("Invalid time format: " + time);
        }
    }
    
    public static void validateAbsoluteTime(String time) {
        if (!ABSOLUTE_TIME_PATTERN.matcher(time).matches()) {
            throw new IllegalArgumentException("Invalid time format: " + time);
        }
    }
}
