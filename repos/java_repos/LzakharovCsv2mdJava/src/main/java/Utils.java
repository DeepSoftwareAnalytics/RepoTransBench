import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static String columnLetter(int index) {
        StringBuilder column = new StringBuilder();
        while (index >= 0) {
            column.insert(0, (char)('a' + index % 26));
            index = index / 26 - 1;
        }
        return column.toString();
    }
    
    public static List<String> makeDefaultHeaders(int numberOfColumns) {
        List<String> headers = new ArrayList<>();
        for (int i = 0; i < numberOfColumns; i++) {
            headers.add(columnLetter(i));
        }
        return headers;
    }
}
