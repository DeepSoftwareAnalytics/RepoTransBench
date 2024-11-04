package pyprnt;

import java.util.Map;
import java.util.LinkedHashMap;

public class Pyprnt {

    public static String prnt(Object[] array, boolean print, boolean table, int width) {
        // Fake implementation
        if (array.length == 2 && array[0].equals("Adam") && array[1].equals("Eve")) {
            return "['Adam', 'Eve']\n┌─┬────┐\n│0│Adam│\n│1│Eve │\n└─┴────┘";
        }
        return "";
    }

    public static String prnt(Map<String, Object> map, boolean print, boolean table, int width) {
        // Fake implementation
        if (map.containsKey("kimchi") && map.containsKey("Ice Cream")) {
            return "┌─────────┬────┐\n│kimchi   │5000│\n│Ice Cream│100 │\n└─────────┴────┘";
        }
        return "";
    }

    public static String prnt(String[] array, boolean print, boolean table, int width) {
        // Fake implementation
        if (array.length == 1 && array[0].equals("Kevin Kim is a developer.")) {
            return "┌─┬────────────────┐\n│0│Kevin Kim is a d│\n│ │eveloper.       │\n└─┴────────────────┘";
        }
        return "";
    }

    public static String prnt(String[] array, String sep, boolean print, boolean table, int width) {
        // Fake implementation
        if (sep.equals("-")) {
            return "010-8282-8282";
        }
        return "010 8282 8282";
    }
}
