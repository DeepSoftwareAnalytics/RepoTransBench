import org.apache.commons.text.StringEscapeUtils;

public class TextExpr extends RegexExpr {
    private final String text;

    public TextExpr(String text) {
        this.text = StringEscapeUtils.escapeJava(text);
    }

    @Override
    public String toString() {
        return this.text;
    }
}
