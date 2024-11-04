import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import pyprnt.Pyprnt;

public class TestPrintTruncate {

    @Test
    public void testTruncateFalse() {
        Object[] input = {"abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz", 12345678910L};
        String testee = Pyprnt.prnt(input, false, true, 50);
        String expect = "┌─┬──────────────────────────────────────────────┐\n│0│abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrst│\n│ │uvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmn│\n│ │opqrstuvwxyzabcdefghijklmnopqrstuvwxyz        │\n│1│12345678910                                   │\n└─┴──────────────────────────────────────────────┘";
        assertEquals(expect, testee);
    }

    @Test
    public void testTruncateTrue() {
        Object[] input = {"abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz", 12345678910L};
        String testee = Pyprnt.prnt(input, true, true, 50);
        String expect = "┌─┬──────────────────────────────────────────────┐\n│0│abcdefghijklmnopqrstuvwxyzabcdefghijklmnopq...│\n│1│12345678910                                   │\n└─┴──────────────────────────────────────────────┘";
        assertEquals(expect, testee);
    }
}
