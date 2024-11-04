import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import ReadTime.API;
import ReadTime.Result;
import ReadTime.Utils;

public class BaseTestCase {

    private static final int DEFAULT_WPM = 265;

    @Test
    public void testTransitions() {
        String word = "word ";
        for (int x = 0; x < 10; x++) {

            // Test the maximum num words for x read time
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < 265 * x; i++) {
                text.append(word);
            }
            ReadTime.Result result = ReadTime.API.ofText(text.toString());
            assertEquals(result.getSeconds(), x * 60 > 0 ? x * 60 : 1);
            assertEquals(result.getText(), (x > 0 ? x : 1) + " min");
            assertEquals(result.toString(), (x > 0 ? x : 1) + " min read");

            // Test the maximum + 1 num words, and make sure read time is x + 1
            text.append("word");
            result = ReadTime.API.ofText(text.toString());
            assertEquals(result.getSeconds(), x * 60 + 1);
            assertEquals(result.getText(), (x + 1) + " min");
            assertEquals(result.toString(), (x + 1) + " min read");
        }
    }

    @Test
    public void testPlainText() throws IOException {
        String inp = new String(Files.readAllBytes(Paths.get("src/test/resources/samples/plain_text.txt")));
        ReadTime.Result result = ReadTime.API.ofText(inp);
        assertEquals(result.getSeconds(), 154);
        assertEquals(result.getText(), "3 min");
        assertEquals(result.toString(), "3 min read");
    }

    @Test
    public void testPlainTextEmpty() {
        ReadTime.Result result = ReadTime.API.ofText("");
        assertEquals(result.getSeconds(), 1);
        assertEquals(result.getText(), "1 min");
        assertEquals(result.toString(), "1 min read");
    }

    @Test
    public void testPlainTextNull() {
        ReadTime.Result result = ReadTime.API.ofText(null);
        assertEquals(result.getSeconds(), 0);
        assertEquals(result.getText(), "1 min");
        assertEquals(result.toString(), "1 min read");
    }

    @Test
    public void testMarkdown() throws IOException {
        String inp = new String(Files.readAllBytes(Paths.get("src/test/resources/samples/markdown.md")));
        ReadTime.Result result = ReadTime.API.ofMarkdown(inp);
        assertEquals(result.getSeconds(), 236);
        assertEquals(result.getText(), "4 min");
        assertEquals(result.toString(), "4 min read");
    }

    @Test
    public void testHtml() throws IOException {
        String inp = new String(Files.readAllBytes(Paths.get("src/test/resources/samples/html.html")));
        ReadTime.Result result = ReadTime.API.ofHtml(inp);
        assertEquals(result.getSeconds(), 236);
        assertEquals(result.getText(), "4 min");
        assertEquals(result.toString(), "4 min read");
    }

    @Test
    public void testPlainTextUnicode() {
        ReadTime.Result result = ReadTime.API.ofText("Some simple text");
        assertEquals(result.toString(), "1 min read");
    }

    @Test
    public void testUnsupportedFormat() {
        Exception exception = assertThrows(Exception.class, () -> {
            ReadTime.Utils.readTime("Some simple text", "foo");
        });
        assertEquals("Unsupported format: foo", exception.getMessage());
    }

    @Test
    public void testInvalidFormat() {
        Exception exception = assertThrows(Exception.class, () -> {
            ReadTime.Utils.readTime("Some simple text", 123);
        });
        assertEquals("Unsupported format: 123", exception.getMessage());
    }

    @Test
    public void testCanAdd() throws IOException {
        String inp = new String(Files.readAllBytes(Paths.get("src/test/resources/samples/plain_text.txt")));
        ReadTime.Result result1 = ReadTime.API.ofText(inp);
        assertEquals(result1.getSeconds(), 154);

        inp = new String(Files.readAllBytes(Paths.get("src/test/resources/samples/markdown.md")));
        ReadTime.Result result2 = ReadTime.API.ofMarkdown(inp);
        assertEquals(result2.getSeconds(), 236);

        ReadTime.Result result = result1.add(result2);
        assertEquals(result.getSeconds(), 154 + 236);
        assertEquals(result.getText(), "7 min");
        assertEquals(result.toString(), "7 min read");
    }

    @Test
    public void testCustomWpm() {
        String text = "some test content ".repeat(100);
        ReadTime.Result result = ReadTime.API.ofText(text);
        assertEquals(result.getWpm(), DEFAULT_WPM);
        assertEquals(result.getSeconds(), 68);
        assertEquals(result.getText(), "2 min");

        int wpm = 50;
        result = ReadTime.API.ofText(text, wpm);
        assertEquals(result.getWpm(), wpm);
        assertEquals(result.getSeconds(), 360);
        assertEquals(result.getText(), "6 min");
        assertEquals(result.toString(), "6 min read");
    }
}
