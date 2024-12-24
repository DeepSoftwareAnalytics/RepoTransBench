import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlPatternTest {

    private final RegexExpr domainName = RegexExpr.oneOrMore(RegexExpr.anyOf(RegexExpr.inRange("a", "z").toString() + RegexExpr.inRange("0", "9").toString() + RegexExpr.text("-").toString()));
    private final RegexExpr domain = domainName.seq(RegexExpr.zeroOrMore(RegexExpr.text(".").seq(domainName)));
    private final RegexExpr pathSegment = RegexExpr.zeroOrMore(RegexExpr.noneOf("/"));
    private final RegexExpr path = RegexExpr.zeroOrMore(RegexExpr.text("/").seq(pathSegment));
    private final RegexExpr url = RegexExpr.group(RegexExpr.oneOrMore(RegexExpr.anyOf(RegexExpr.inRange("a", "z").toString())), "scheme", true)
            .seq(RegexExpr.text("://"))
            .seq(RegexExpr.group(domain, "domain", true))
            .seq(RegexExpr.group(path, "path", true));

    @Test
    public void testUrlPattern() {
        assertEquals(url.toString(),
            "(?P<scheme>[a-z]+)://(?P<domain>[a-z0-9\\-]+(?:\\.[a-z0-9\\-]+)*)(?P<path>(?:/[^/]*)*)");

        Pattern pattern = Pattern.compile(url.toString());

        Matcher matcher = pattern.matcher("://foo");
        assertEquals("", matcher.matches() ? null : "");

        matcher = pattern.matcher("https://google.com");
        if (matcher.matches()) {
            assertEquals("scheme=https, domain=google.com, path=", matcher.group("scheme") + "=" + matcher.group("scheme") + ", " +
                    matcher.group("domain") + "=" + matcher.group("domain") + ", " +
                    matcher.group("path") + "=" + matcher.group("path"));
        }

        matcher = pattern.matcher("https://google.com/test/1/2/3");
        if (matcher.matches()) {
            assertEquals("scheme=https, domain=google.com, path=/test/1/2/3", matcher.group("scheme") + "=" + matcher.group("scheme") + ", " +
                    matcher.group("domain") + "=" + matcher.group("domain") + ", " +
                    matcher.group("path") + "=" + matcher.group("path"));
        }
    }
}
