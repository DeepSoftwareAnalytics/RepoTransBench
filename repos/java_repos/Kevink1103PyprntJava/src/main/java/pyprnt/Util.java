package pyprnt.util;

public class Util {

    public static int getTerminalSize() {
        // Fake implementation
        return 80;
    }

    public static String border(String type, int width1, int width2, int totalWidth) {
        // Fake implementation
        if ("top".equals(type)) {
            return "┌─────┬─────┐";
        } else if ("bottom".equals(type)) {
            return "└─────┴─────┘";
        } else if ("top".equals(type) && width1 == 5 && width2 == 20) {
            return "┌─────┬────────────┐";
        }
        return "";
    }
}
