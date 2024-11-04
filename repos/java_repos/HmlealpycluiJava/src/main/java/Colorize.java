public class Colorize {

    private String colorize(String colorCode, String text) {
        return "\033[" + colorCode + "m" + text + "\33[0m";
    }

    public String grey(String text) {
        return colorize("30;1", text);
    }

    public String red(String text) {
        return colorize("31", text);
    }

    public String green(String text) {
        return colorize("32", text);
    }

    public String yellow(String text) {
        return colorize("33", text);
    }

    public String blue(String text) {
        return colorize("34", text);
    }

    public String pink(String text) {
        return colorize("35", text);
    }

    public String light_blue(String text) {
        return colorize("36", text);
    }
}
