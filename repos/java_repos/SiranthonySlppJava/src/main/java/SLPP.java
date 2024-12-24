import java.util.*;
import java.util.regex.*;

public class SLPP {

    private static final Map<String, String> ERRORS = new HashMap<>();
    static {
        ERRORS.put("unexp_end_string", "Unexpected end of string while parsing Lua string.");
        ERRORS.put("unexp_end_table", "Unexpected end of table while parsing Lua string.");
        ERRORS.put("mfnumber_minus", "Malformed number (no digits after initial minus).");
        ERRORS.put("mfnumber_dec_point", "Malformed number (no digits after decimal point).");
        ERRORS.put("mfnumber_sci", "Malformed number (bad scientific format).");
    }

    private String text;
    private char ch;
    private int at;
    private int len;
    private int depth;
    private Pattern space = Pattern.compile("\\s", Pattern.MULTILINE);
    private Pattern alnum = Pattern.compile("\\w", Pattern.MULTILINE);
    private String newline = "\n";
    private String tab = "\t";

    public Object decode(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        this.text = text;
        this.at = 0;
        this.ch = '\0';
        this.depth = 0;
        this.len = text.length();
        nextChar();
        return value();
    }

    public String encode(Object obj) {
        this.depth = 0;
        return encodeInternal(obj);
    }

    private String encodeInternal(Object obj) {
        StringBuilder s = new StringBuilder();
        String tab = this.tab;
        String newline = this.newline;

        if (obj instanceof String) {
            s.append("\"").append(((String) obj).replace("\"", "\\\"")).append("\"");
        } else if (obj instanceof Boolean) {
            s.append(obj.toString().toLowerCase());
        } else if (obj == null) {
            s.append("nil");
        } else if (obj instanceof Number) {
            s.append(obj.toString());
        } else if (obj instanceof Collection || obj instanceof Map) {
            this.depth++;
            if (obj instanceof Collection && ((Collection<?>) obj).isEmpty()) {
                newline = tab = "";
            }
            String dp = tab.repeat(this.depth);
            s.append(String.format("%s{%s", tab.repeat(this.depth - 2), newline));
            if (obj instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) obj;
                List<String> contents = new ArrayList<>();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    contents.add(dp + String.format("[\"%s\"] = %s", entry.getKey(), encodeInternal(entry.getValue())));
                }
                s.append(String.join("," + newline, contents));
            } else {
                Collection<?> collection = (Collection<?>) obj;
                List<String> contents = new ArrayList<>();
                for (Object el : collection) {
                    contents.add(dp + encodeInternal(el));
                }
                s.append(String.join("," + newline, contents));
            }
            this.depth--;
            s.append(String.format("%s%s}", newline, tab.repeat(this.depth)));
        }
        return s.toString();
    }

    private void white() {
        while (this.ch != '\0') {
            if (space.matcher(Character.toString(this.ch)).matches()) {
                nextChar();
            } else {
                break;
            }
        }
        comment();
    }

    private void comment() {
        if (this.ch == '-' && nextIs('-')) {
            nextChar();
            boolean multiline = nextChar() && this.ch == '[' && nextIs('[');
            while (this.ch != '\0') {
                if (multiline) {
                    if (this.ch == ']' && nextIs(']')) {
                        nextChar();
                        nextChar();
                        white();
                        break;
                    }
                } else if (this.ch == '\n') {
                    white();
                    break;
                }
                nextChar();
            }
        }
    }

    private boolean nextIs(char value) {
        return this.at < this.len && this.text.charAt(this.at) == value;
    }

    private boolean nextChar() {
        if (this.at >= this.len) {
            this.ch = '\0';
            return false;
        }
        this.ch = this.text.charAt(this.at);
        this.at++;
        return true;
    }

    private Object value() {
        white();
        if (this.ch == '\0') {
            return null;
        }
        if (this.ch == '{') {
            return object();
        }
        if (this.ch == '"') {
            return string('"');
        }
        if (this.ch == '\'') {
            return string('\'');
        }
        if (Character.isDigit(this.ch) || this.ch == '-') {
            return number();
        }
        return word();
    }

    private String string(char end) {
        StringBuilder s = new StringBuilder();
        char start = this.ch;
        boolean doubleBracket = start == '[' && prevIs(start);
        while (nextChar()) {
            if (this.ch == end && (!doubleBracket || nextIs(end))) {
                nextChar();
                if (start != '[' || this.ch == ']') {
                    if (doubleBracket) {
                        nextChar();
                    }
                    return s.toString();
                }
            }
            if (this.ch == '\\' && start == end) {
                nextChar();
                if (this.ch != end) {
                    s.append('\\');
                }
            }
            s.append(this.ch);
        }
        throw new ParseException(ERRORS.get("unexp_end_string"));
    }

    private Map<Object, Object> object() {
        Map<Object, Object> o = new HashMap<>();
        Object key = null;
        int index = 0;
        boolean numericKeys = false;
        this.depth++;
        nextChar();
        white();
        if (this.ch == '}') {
            this.depth--;
            nextChar();
            return o;
        }
        while (this.ch != '\0') {
            white();
            if (this.ch == '{') {
                o.put(index, object());
                index++;
                continue;
            } else if (this.ch == '}') {
                this.depth--;
                nextChar();
                if (key != null) {
                    o.put(index, key);
                }
                if (o.keySet().stream().allMatch(k -> k instanceof Number)) {
                    List<Integer> sortedKeys = new ArrayList<>();
                    for (Object k : o.keySet()) {
                        sortedKeys.add((Integer) k);
                    }
                    Collections.sort(sortedKeys);
                    List<Object> array = new ArrayList<>();
                    for (Integer k : sortedKeys) {
                        array.add(o.get(k));
                    }
                    return new HashMap<Object, Object>() {{
                        put("array", array);
                    }};
                }
                return o;
            } else if (this.ch == ',') {
                nextChar();
                continue;
            } else {
                key = value();
                if (this.ch == ']') {
                    nextChar();
                }
                white();
                char ch = this.ch;
                if (ch == '=' || ch == ',') {
                    nextChar();
                    white();
                    if (ch == '=') {
                        o.put(key, value());
                    } else {
                        o.put(index, key);
                    }
                    index++;
                    key = null;
                }
            }
        }
        throw new ParseException(ERRORS.get("unexp_end_table"));
    }

    private static final Map<String, Object> WORDS = new HashMap<String, Object>() {{
        put("true", true);
        put("false", false);
        put("nil", null);
    }};

    private Object word() {
        StringBuilder s = new StringBuilder();
        if (this.ch != '\n') {
            s.append(this.ch);
        }
        nextChar();
        while (this.ch != '\0' && alnum.matcher(Character.toString(this.ch)).matches() && !WORDS.containsKey(s.toString())) {
            s.append(this.ch);
            nextChar();
        }
        return WORDS.getOrDefault(s.toString(), s.toString());
    }

    private Object number() {
        StringBuilder n = new StringBuilder();
        try {
            if (this.ch == '-') {
                n.append(this.ch);
                nextChar();
                if (!Character.isDigit(this.ch)) {
                    throw new ParseException(ERRORS.get("mfnumber_minus"));
                }
            }
            n.append(digit());
            if (n.toString().equals("0") && (this.ch == 'x' || this.ch == 'X')) {
                n.append(this.ch);
                nextChar();
                n.append(hex());
            } else {
                if (this.ch == '.') {
                    n.append(this.ch);
                    nextChar();
                    n.append(digit());
                }
                if (this.ch == 'e' || this.ch == 'E') {
                    n.append(this.ch);
                    nextChar();
                    if (this.ch != '+' && this.ch != '-') {
                        throw new ParseException(ERRORS.get("mfnumber_sci"));
                    }
                    n.append(this.ch);
                    nextChar();
                    n.append(digit());
                }
            }
            return Integer.parseInt(n.toString());
        } catch (NumberFormatException e) {
            return Double.parseDouble(n.toString());
        }
    }

    private String digit() {
        StringBuilder n = new StringBuilder();
        while (this.ch != '\0' && Character.isDigit(this.ch)) {
            n.append(this.ch);
            nextChar();
        }
        return n.toString();
    }

    private String hex() {
        StringBuilder n = new StringBuilder();
        while (this.ch != '\0' && (Character.isDigit(this.ch) || "ABCDEFabcdef".indexOf(this.ch) >= 0)) {
            n.append(this.ch);
            nextChar();
        }
        return n.toString();
    }

    private boolean prevIs(char value) {
        return this.at >= 2 && this.text.charAt(this.at - 2) == value;
    }

    public static void main(String[] args) {
        SLPP slpp = new SLPP();
        Map<String, Object> decoded = (Map<String, Object>) slpp.decode("{\"key1\":1, \"key2\":\"value2\"}");
        System.out.println("Decoded: " + decoded);
        String encoded = slpp.encode(decoded);
        System.out.println("Encoded: " + encoded);
    }

    public static class ParseException extends RuntimeException {
        public ParseException(String message) {
            super(message);
        }
    }
}
