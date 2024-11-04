package ReadTime;

public class API {

    public static Result ofText(String text) {
        if (text == null) {
            return new Result(0, Utils.DEFAULT_WPM);
        }
        return ofText(text, Utils.DEFAULT_WPM);
    }

    public static Result ofText(String text, int wpm) {
        try {
            return Utils.readTime(text, "text", wpm);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Result ofHtml(String html) {
        return ofHtml(html, Utils.DEFAULT_WPM);
    }

    public static Result ofHtml(String html, int wpm) {
        try {
            return Utils.readTime(html, "html", wpm);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Result ofMarkdown(String markdown) {
        return ofMarkdown(markdown, Utils.DEFAULT_WPM);
    }

    public static Result ofMarkdown(String markdown, int wpm) {
        try {
            return Utils.readTime(markdown, "markdown", wpm);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
