import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Color {
    static final List<String> COLORS = Arrays.asList(
            "black", "brown", "green", "purple", "yellow",
            "blue", "gray", "orange", "red", "white"
    );

    private static final Pattern PATTERN = Pattern.compile(
            "0x[0-9A-Fa-f]{6}|[0-9A-Fa-f]{8}"
    );

    public static boolean isValidColor(String color) {
        return COLORS.contains(color) || PATTERN.matcher(color).matches();
    }
}
