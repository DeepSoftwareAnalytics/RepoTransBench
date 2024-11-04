package ReadTime;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static final int DEFAULT_WPM = 265;
    private static final Pattern WORD_DELIMITER = Pattern.compile("\\W+");

    public static Result readTime(String content, String format) throws Exception {
        return readTime(content, format, DEFAULT_WPM);
    }

    public static Result readTime(String content, String format, int wpm) throws Exception {
        if (format == null) {
            throw new IllegalArgumentException("Format is null");
        }

        format = format.toLowerCase();

        int seconds;
        switch (format) {
            case "text":
                seconds = readTimeAsSeconds(content, 0, wpm);
                break;
            case "markdown":
                Document docMarkdown = Jsoup.parse(markdownToHtml(content));
                int imagesMarkdown = countImages(docMarkdown);
                seconds = readTimeAsSeconds(docMarkdown.text(), imagesMarkdown, wpm);
                break;
            case "html":
                Document docHtml = Jsoup.parse(content);
                int imagesHtml = countImages(docHtml);
                seconds = readTimeAsSeconds(docHtml.text(), imagesHtml, wpm);
                break;
            default:
                throw new Exception("Unsupported format: " + format);
        }

        return new Result(seconds, wpm);
    }

    private static int countImages(Document doc) {
        Elements imgTags = doc.select("img");
        return imgTags.size();
    }

    private static String markdownToHtml(String markdown) {
        // Simple conversion for demonstration purposes
        return markdown.replaceAll("^(#{1,6})\\s*(.*)$", "<h1>$2</h1>");
    }

    public static int readTimeAsSeconds(String text, int images, int wpm) {
        if (wpm == 0) {
            wpm = DEFAULT_WPM;
        }

        Matcher matcher = WORD_DELIMITER.matcher(text.trim());
        int numWords = 0;
        while (matcher.find()) {
            numWords++;
        }

        int seconds = (int) Math.ceil((double) numWords / wpm * 60);

        int delta = 12;
        for (int i = 0; i < images; i++) {
            seconds += delta;
            if (delta > 3) {
                delta--;
            }
        }

        return seconds;
    }

    public static Result readTime(String content, int format) throws Exception {
        throw new Exception("Unsupported format: " + format);
    }
}
