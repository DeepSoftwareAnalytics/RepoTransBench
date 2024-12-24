import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.regex.*;
import java.util.function.*;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class TitleCaseTest {

    private TitleCase titlecase = new TitleCase();

    private static final Object[][] TEST_DATA = new Object[][]{
        {"", ""},
        {"word/word", "Word/Word"},
        {"a title and/or string", "A Title and/or String"},
        {"dance with me/let’s face the music and dance", "Dance With Me/Let’s Face the Music and Dance"},
        {"a-b end-to-end two-not-three/three-by-four/five-and", "A-B End-to-End Two-Not-Three/Three-by-Four/Five-And"},
        {"34th 3rd 2nd", "34th 3rd 2nd"},
        {"Q&A with steve jobs: 'that's what happens in technology'", "Q&A With Steve Jobs: 'That's What Happens in Technology'"},
        {"What is AT&T's problem?", "What Is AT&T's Problem?"},
        {"Apple deal with AT&T falls through", "Apple Deal With AT&T Falls Through"},
        {"Words with all consonants like cnn are acronyms", "Words With All Consonants Like CNN Are Acronyms"},
        {"this v that", "This v That"},
        {"this v. that", "This v. That"},
        {"this vs that", "This vs That"},
        {"this vs. that", "This vs. That"},
        {"The SEC's Apple probe: what you need to know", "The SEC's Apple Probe: What You Need to Know"},
        {"'by the Way, small word at the start but within quotes.'", "'By the Way, Small Word at the Start but Within Quotes.'"},
        {"Small word at end is nothing to be afraid of", "Small Word at End Is Nothing to Be Afraid Of"},
        {"Starting Sub-Phrase With a Small Word: a Trick, Perhaps?", "Starting Sub-Phrase With a Small Word: A Trick, Perhaps?"},
        {"Sub-Phrase With a Small Word in Quotes: 'a Trick, Perhaps?'", "Sub-Phrase With a Small Word in Quotes: 'A Trick, Perhaps?'"},
        {"sub-phrase with a small word in quotes: \"a trick, perhaps?\"", "Sub-Phrase With a Small Word in Quotes: \"A Trick, Perhaps?\""},
        {"Starting a Hyphen Delimited Sub-Phrase With a Small Word - a Trick, Perhaps?", "Starting a Hyphen Delimited Sub-Phrase With a Small Word - A Trick, Perhaps?"},
        {"Hyphen Delimited Sub-Phrase With a Small Word in Quotes - 'a Trick, Perhaps?'", "Hyphen Delimited Sub-Phrase With a Small Word in Quotes - 'A Trick, Perhaps?'"},
        {"Hyphen Delimited sub-phrase with a small word in quotes - \"a trick, perhaps?\"", "Hyphen Delimited Sub-Phrase With a Small Word in Quotes - \"A Trick, Perhaps?\""},
        {"Snakes on a Plane - The TV Edit - The Famous Line", "Snakes on a Plane - The TV Edit - The Famous Line"},
        {"Starting an Em Dash Delimited Sub-Phrase With a Small Word — a Trick, Perhaps?", "Starting an Em Dash Delimited Sub-Phrase With a Small Word — A Trick, Perhaps?"},
        {"Em Dash Delimited Sub-Phrase With a Small Word in Quotes — 'a Trick, Perhaps?'", "Em Dash Delimited Sub-Phrase With a Small Word in Quotes — 'A Trick, Perhaps?'"},
        {"Em Dash Delimited sub-phrase with a small word in quotes — \"a trick, perhaps?\"", "Em Dash Delimited Sub-Phrase With a Small Word in Quotes — \"A Trick, Perhaps?\""},
        {"Snakes on a Plane — The TV Edit — The Famous Line", "Snakes on a Plane — The TV Edit — The Famous Line"},
        {"EPISODE 7 — THE FORCE AWAKENS", "Episode 7 — The Force Awakens"},
        {"episode 7 – The force awakens", "Episode 7 – The Force Awakens"},
        {"THE CASE OF X ≤ 7", "The Case of X ≤ 7"},
        {"the case of X ≤ 7", "The Case of X ≤ 7"},
        {"\"Nothing to Be Afraid of?\"", "\"Nothing to Be Afraid Of?\""},
        {"\"Nothing to be Afraid Of?\"", "\"Nothing to Be Afraid Of?\""},
        {"a thing", "A Thing"},
        {"2lmc Spool: 'gruber on OmniFocus and vapo(u)rware'", "2lmc Spool: 'Gruber on OmniFocus and Vapo(u)rware'"},
        {"this is just an example.com", "This Is Just an example.com"},
        {"this is something listed on del.icio.us", "This Is Something Listed on del.icio.us"},
        {"iTunes should be unmolested", "iTunes Should Be Unmolested"},
        {"reading between the lines of steve jobs’s ‘thoughts on music’", "Reading Between the Lines of Steve Jobs’s ‘Thoughts on Music’"},
        {"seriously, ‘repair permissions’ is voodoo", "Seriously, ‘Repair Permissions’ Is Voodoo"},
        {"generalissimo francisco franco: still dead; kieren McCarthy: still a jackass", "Generalissimo Francisco Franco: Still Dead; Kieren McCarthy: Still a Jackass"},
        {"O'Reilly should be untouched", "O'Reilly Should Be Untouched"},
        {"my name is o'reilly", "My Name Is O'Reilly"},
        {"WASHINGTON, D.C. SHOULD BE FIXED BUT MIGHT BE A PROBLEM", "Washington, D.C. Should Be Fixed but Might Be a Problem"},
        {"THIS IS ALL CAPS AND SHOULD BE ADDRESSED", "This Is All Caps and Should Be Addressed"},
        {"Mr McTavish went to MacDonalds", "Mr McTavish Went to MacDonalds"},
        {"this shouldn't\nget mangled", "This Shouldn't\nGet Mangled"},
        {"this is http://foo.com", "This Is http://foo.com"},
        {"mac mc MAC MC machine", "Mac Mc MAC MC Machine"},
        {"FOO BAR 5TH ST", "Foo Bar 5th St"},
        {"foo bar 5th st", "Foo Bar 5th St"},
        {"l'grange l'grange l'Grange l'Grange", "l'Grange l'Grange l'Grange l'Grange"},
        {"L'grange L'grange L'Grange L'Grange", "l'Grange l'Grange l'Grange l'Grange"},
        {"l'GranGe", "l'GranGe"},
        {"o'grange O'grange o'Grange O'Grange", "O'Grange O'Grange O'Grange O'Grange"},
        {"o'grange's O'grange's o'Grange's O'Grange's", "O'Grange's O'Grange's O'Grange's O'Grange's"},
        {"O'GranGe", "O'GranGe"},
        {"o'melveny/o'doyle o'Melveny/o'doyle O'melveny/o'doyle o'melveny/o'Doyle o'melveny/O'doyle", "O'Melveny/O'Doyle O'Melveny/O'Doyle O'Melveny/O'Doyle O'Melveny/O'Doyle O'Melveny/O'Doyle"},
        {"mccay-mcbut-mcdo mcdonalds/mcby", "McCay-McBut-McDo McDonalds/McBy"},
        {"oblon, spivak, mcclelland, maier & neustadt", "Oblon, Spivak, McClelland, Maier & Neustadt"},
        {"Mcoblon, spivak, mcclelland, mcmaier, & mcneustadt", "McOblon, Spivak, McClelland, McMaier, & McNeustadt"},
        {"mcfoo-bar, MCFOO-BAR, McFoo-bar, McFoo-Bar, mcfoo-mcbar, foo-mcbar", "McFoo-Bar, McFoo-Bar, McFoo-Bar, McFoo-Bar, McFoo-McBar, Foo-McBar"},
        {"'QUOTE' A GREAT", "'Quote' a Great"},
        {"‘QUOTE’ A GREAT", "‘Quote’ a Great"},
        {"“YOUNG AND RESTLESS”", "“Young and Restless”"},
        {"EL NIÑO A ARRIVÉ HIER", "El Niño a Arrivé Hier"},
        {"YEA NO", "Yea No"},
        {"ÝÆ ÑØ", "Ýæ Ñø"},
        {"yea no", "Yea No"},
        {"ýæ ñø", "Ýæ Ñø"},
        {"Mr mr Mrs Ms Mss Dr dr , Mr. and Mrs. Person", "Mr Mr Mrs Ms MSS Dr Dr , Mr. And Mrs. Person"},
        {"a mix of\tdifferent\u200aspace\u2006characters", "A Mix of\tDifferent\u200aSpace\u2006Characters"}
    };

    @Test
    public void testTitleCase() {
        for (Object[] data : TEST_DATA) {
            assertEquals(data[1], titlecase.titleCase((String) data[0]));
        }
    }

    @Test
    public void testUpperCaseInitialsRegex() {
        Pattern UC_INITIALS = Pattern.compile("^(?:[A-Z]\\.|[A-Z]\\.||[A-Z]\\.[A-Z])+$");
        assertTrue(UC_INITIALS.matcher("A.B").matches());
        assertTrue(UC_INITIALS.matcher("A.B.").matches());
        assertFalse(UC_INITIALS.matcher("ABCD").matches());
    }

    @Test
    public void testAtNT() {
        BiFunction<String, Map<String, Object>, String> callback = (word, caps) -> {
            if ("AT&T".equalsIgnoreCase(word)) {
                return word.toUpperCase();
            }
            return null;
        };
        assertEquals("AT&T", titlecase.titleCase("at&t", callback, true));
    }

    @Test
    public void testCallback() {
        BiFunction<String, Map<String, Object>, String> abbreviation = (word, caps) -> {
            if ("TCP".equalsIgnoreCase(word) || "UDP".equalsIgnoreCase(word)) {
                return word.toUpperCase();
            }
            return null;
        };

        String s = "a simple tcp and udp wrapper";
        assertEquals("A Simple Tcp and Udp Wrapper", titlecase.titleCase(s));
        assertEquals("A Simple TCP and UDP Wrapper", titlecase.titleCase(s, abbreviation, true));
        assertEquals("A Simple TCP and UDP Wrapper", titlecase.titleCase(s.toUpperCase(), abbreviation, true));
        assertEquals("CRÈME BRÛLÉE", titlecase.titleCase("crème brûlée", (word, caps) -> word.toUpperCase(), true));
    }

    @Test
    public void testCustomAbbreviations() throws IOException {
        File tempFile = File.createTempFile("abbreviations", ".txt");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {
            bw.write("UDP\nPPPoE\n");
        }

        Function<String, String> wordlistFilter = titlecase.createWordlistFilterFromFile(tempFile.getAbsolutePath());

        assertEquals("Sending UDP Packets Over PPPoE Works Great", titlecase.titleCase("sending UDP packets over PPPoE works great"));
        assertEquals("Sending Udp Packets Over Pppoe Works Great", titlecase.titleCase("SENDING UDP PACKETS OVER PPPOE WORKS GREAT"));
        assertEquals("Sending UDP Packets Over PPPoE Works Great", titlecase.titleCase("sending UDP packets over PPPoE works great", (word, caps) -> wordlistFilter.apply(word), true));
    }
}
