import java.util.regex.Pattern;

public class HeaderMatcher {

    private static final String DEFAULT_HEADER_TEMPLATE =
            "//\n" +
            "//.*\\..*\n" +
            "//.*\n" +
            "//\n" +
            "//\\s{2}Created by\\s.*\\son\\s\\d{1,2}\\/\\d{1,2}\\/\\d{2}\\.\\n" +
            "//\\s{2}Copyright\\s(\\(c\\)|©)\\s\\d{4}\\s.*\\.\\sAll rights reserved\\.\\n" +
            "//\n";

    private final String content;
    private final boolean trimNewLines;

    public HeaderMatcher(String content, boolean trimNewLines) {
        this.content = content;
        this.trimNewLines = trimNewLines;
    }

    private String getHeader() {
        String trimRegex = trimNewLines ? "\\s*" : "";
        return String.format("%s%s%s", trimRegex, DEFAULT_HEADER_TEMPLATE, trimRegex);
    }

    public String match() {
        String headerPattern = getHeader();
        Pattern pattern = Pattern.compile(headerPattern);
        java.util.regex.Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(0) : null; // Use 'matcher.find()' instead of 'matcher.matches()'
    }
}
