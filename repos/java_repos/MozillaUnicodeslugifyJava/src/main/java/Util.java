import com.ibm.icu.text.Normalizer2;
import com.ibm.icu.text.UnicodeSet;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Util {
    public static String smartText(Object s, String encoding, String errors) {
        if (s instanceof String) {
            return (String) s;
        }

        if (s instanceof byte[]) {
            return new String((byte[]) s, StandardCharsets.UTF_8);
        }

        return String.valueOf(s);
    }

    public static String sanitize(String text, String ok) {
        StringBuilder rv = new StringBuilder();
        UnicodeSet okChars = new UnicodeSet(ok);
        UnicodeSet lettersAndNumbers = new UnicodeSet("[[:L:][:N:]]");

        for (char c : text.toCharArray()) {
            if (lettersAndNumbers.contains(c) || okChars.contains(c)) {
                rv.append(c);
            } else if (Character.isWhitespace(c)) {
                rv.append(' ');
            }
        }

        return rv.toString().strip();
    }

    private static final Pattern SPACE_PATTERN = Pattern.compile("[\\s]+");

    public static final String SLUG_OK = "-_~";

    public static String slugify(String s, String ok, boolean lower, boolean spaces, boolean onlyAscii, String spaceReplacement) {
        if (onlyAscii && !ok.equals(SLUG_OK)) {
            for (char c : ok.toCharArray()) {
                if (c > 127) {
                    throw new IllegalArgumentException("You can not use \"onlyAscii=true\" with a non-ASCII characters in \"ok\" (\"" + ok + "\" given)");
                }
            }
        }

        Normalizer2 normalizer = Normalizer2.getNFKCInstance();
        String newText = sanitize(normalizer.normalize(smartText(s, "UTF-8", "strict")), ok);

        if (onlyAscii) {
            newText = sanitize(smartText(newText, "ASCII", "strict"), ok);
        }

        if (!spaces) {
            if (spaceReplacement != null && !ok.contains(spaceReplacement)) {
                spaceReplacement = ok.isEmpty() ? "" : String.valueOf(ok.charAt(0));
            }

            newText = SPACE_PATTERN.matcher(newText).replaceAll(spaceReplacement);
        }

        if (lower) {
            newText = newText.toLowerCase();
        }

        return newText;
    }
}
