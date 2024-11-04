import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {

    private void checkSlugify(String input, String expected) {
        assertEquals(expected, Util.slugify(input, Util.SLUG_OK, true, false, false, "-"));
    }

    private void checkOnlyAscii(String input, String expected) {
        assertEquals(expected, Util.slugify(input, Util.SLUG_OK, true, false, true, "-"));
    }

    private void checkOnlyAsciiCapital(String input, String expected) {
        assertEquals(expected, Util.slugify(input, Util.SLUG_OK, false, false, true, "-"));
    }

    private void checkOnlyAsciiLowerNoSpaces(String input, String expected) {
        assertEquals(expected, Util.slugify(input, Util.SLUG_OK, true, false, true, "-"));
    }

    private void checkOkChars(String input, String expected) {
        assertEquals(expected, Util.slugify(input, "-♰é_è", true, false, false, "-"));
    }

    private void checkEmptyOkChars(String input, String expected) {
        assertEquals(expected, Util.slugify(input, "", true, false, false, "-"));
    }

    private void checkLimitedOkCharsOnlyAscii(String input, String expected) {
        assertEquals(expected, Util.slugify(input, "-", true, false, true, "-"));
    }

    @Test
    void testSlugify() {
        String u = "Ελληνικά";
        String x = String.join("-", u, u);
        String y = String.join(" - ", u, u);

        // Slugify test cases
        checkSlugify("xx x  - \"#$@ x", "xx-x-x");
        checkSlugify("Bän...g (bang)", "bäng-bang");
        checkSlugify(u, u.toLowerCase());
        checkSlugify(x, x.toLowerCase());
        checkSlugify(y, x.toLowerCase());
        checkSlugify("    a ", "a");
        checkSlugify("tags/", "tags");
        checkSlugify("holy_wars", "holy_wars");
        checkSlugify("el niño", "el-niño");
        checkSlugify("el niño", "el-niño");
        checkSlugify("films", "films");
        checkSlugify("x𘍿", "x");
        checkSlugify("ϧ΃𘒬𘓣",  "ϧ");
        checkSlugify("¿x", "x");
        checkSlugify("Bakıcı geldi", "bak\u0131c\u0131-geldi");
        checkSlugify("Bäuma means tree", "bäuma-means-tree");

        // Only ASCII test cases
        checkOnlyAscii("Bakıcı geldi", "bakici-geldi");
        checkOnlyAscii("Bäuma means tree", "bauma-means-tree");
        checkOnlyAscii("земельного", "zemelnogo");
        checkOnlyAscii("123 test 朝阳区", "123-test-zhao-yang-qu");

        // Only ASCII capital test cases
        checkOnlyAsciiCapital("BÄUMA MEANS TREE", "BAUMA-MEANS-TREE");
        checkOnlyAsciiCapital("EMİN WAS HERE", "EMIN-WAS-HERE");

        // Only ASCII lower no spaces test cases
        checkOnlyAsciiLowerNoSpaces("北京 (China)", "bei-jing-china");
        checkOnlyAsciiLowerNoSpaces("   Москва (Russia)   ", "moskva-russia");
        checkOnlyAsciiLowerNoSpaces("♰ Vlad ♰ Țepeș ♰", "vlad-tepes");
        checkOnlyAsciiLowerNoSpaces("   ☂   Umbrella   Corp.   ☢   ", "umbrella-corp");
        checkOnlyAsciiLowerNoSpaces("~   breaking   space   ~", "breaking-space");

        // Ok chars test cases
        checkOkChars("-♰é_è ok but not ☢~", "-♰é_è-ok-but-not");
        checkOkChars("♰ Vlad ♰ Țepeș ♰", "♰-vlad-♰-țepeș-♰");
        checkOkChars("   ☂   Umbrella   Corp.   ☢   ", "umbrella-corp");
        checkOkChars("~   breaking   space   ~", "breaking-space");

        // Empty ok chars test cases
        checkEmptyOkChars("-♰no th ing ☢~", "nothing");
        checkEmptyOkChars("♰ Vlad ♰ Țepeș ♰", "vladțepeș");
        checkEmptyOkChars("   ☂   Umbrella   Corp.   ☢   ", "umbrellacorp");
        checkEmptyOkChars("~   breaking   space   ~", "breakingspace");

        // Limited ok chars only ASCII test cases
        checkLimitedOkCharsOnlyAscii("♰é_è ☢~", "ee");
        checkLimitedOkCharsOnlyAscii("♰ Vlad ♰ Țepeș ♰", "vlad-tepes");
        checkLimitedOkCharsOnlyAscii("   ☂   Umbrella   Corp.   ☢   ", "umbrella-corp");
        checkLimitedOkCharsOnlyAscii("~   breaking   space   ~", "breaking-space");

        // Custom space replacement test cases
        assertEquals(Util.slugify("-☀- pretty waves under the sunset 😎", Util.SLUG_OK, true, false, false, "~"), "--~pretty~waves~under~the~sunset");
        assertEquals(Util.slugify("-☀- pretty waves under the sunset 😎", "~", true, false, false, "-"), "pretty~waves~under~the~sunset");
    }

    @Test
    void testSmartTextCase() {
        class MyString {

            @Override
            public String toString() {
                return "\u00F6\u00E4\u00FC";
            }

            @SuppressWarnings("unused")
            public String toUnicode() {
                return toString();
            }
        }

        Exception exception = assertThrows(RuntimeException.class, () -> Util.smartText(new MyString(), "UTF-8", "strict"));
        assertNotNull(exception);

        class TestClass {
            @Override
            public String toString() {
                return "ŠĐĆŽćžšđ";
            }

            public byte[] toBytes() {
                return "Foo".getBytes(StandardCharsets.UTF_8);
            }
        }

        assertEquals("ŠĐĆŽćžšđ", Util.smartText(new TestClass(), "UTF-8", "strict"));
        assertEquals("1", Util.smartText(1, "UTF-8", "strict"));
        assertEquals("foo", Util.smartText("foo", "UTF-8", "strict"));
        assertEquals("Ελληνικά", Util.smartText("Ελληνικά", "UTF-8", "strict"));
    }
}
