import java.util.*;

public class Gauge {

    private Colorize c = new Colorize();

    public String gauge(int value, int maxValue, int width, int dangerZone) {
        return gauge(value, maxValue, width, dangerZone, null);
    }

    public String gauge(int value, int maxValue, int width, int dangerZone, String suffix) {
        if (maxValue == 0) {
            return "[]";
        }

        int length = (int) Math.ceil((double) value / maxValue * width);

        if (length > width) {
            length = width;
        }

        StringBuilder bars = new StringBuilder();
        for (int i = 0; i < length; i++) {
            bars.append("|");
        }

        if (value > dangerZone) {
            bars = new StringBuilder(c.red(bars.toString()));
        }
        bars = new StringBuilder(c.green(bars.toString()));
        for (int i = 0; i < (width + 1 - length); i++) {
            bars.append("-");
        }

        return String.format("[%s] %s", bars.toString(), suffix == null ? "" : suffix);
    }
}
