import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;

public class RegexTests {

    private String regexRecognise(String js) {
        StringWriter output = new StringWriter();
        try {
            StringReader input = new StringReader(js.substring(2)); // 跳过前两个字符
            JavascriptMinify minifier = new JavascriptMinify(input, output, "'\"");
            minifier.regexLiteral(js.charAt(0), js.charAt(1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    private void assertRegex(String jsInput, String expected) {
        assert jsInput.charAt(0) == '/'; // 确保输入是正则表达式
        String recognised = regexRecognise(jsInput);
        assertEquals(String.format("\n in: %s\ngot: %s\nexp: %s", jsInput, recognised, expected), expected, recognised);
    }

    @Test
    public void testSimple() {
        assertRegex("/123/g", "/123/");
    }

    @Test
    public void testCharacterClass() {
        assertRegex("/a[0-9]b/g", "/a[0-9]b/");
    }

    @Test
    public void testCharacterClassWithSlash() {
        assertRegex("/a[/]b/g", "/a[/]b/");
    }

    @Test
    public void testEscapedForwardSlash() {
        assertRegex("/a\\/b/g", "/a\\/b/");
    }

    @Test
    public void testEscapedBackSlash() {
        assertRegex("/a\\\\/g", "/a\\\\/");
    }

    @Test
    public void testEmptyCharacterClass() {
        // 解释：空字符类在大多数正则表达式实现中是不合法的。
        // 所以这个字符类被解释为包含 ]/ ，而不是 [] 后跟 /。
        assertRegex("/a[]/]b/g", "/a[]/]b/");
    }

    @Test
    public void testPrecedenceOfParens() {
        // () 括号的优先级低于 []
        assertRegex("/a([)])b/g", "/a([)])b/");
        assertRegex("/a[(]b/g", "/a[(]b/");
    }
}
