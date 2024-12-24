import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import pyprnt.Pyprnt;

public class TestPrintEnd {

    @Test
    public void testEndEmpty() {
        String testee = Pyprnt.prnt(new String[]{"010", "8282", "8282"}, "", true, true, 50);
        String expect = "010 8282 8282";
        assertEquals(expect, testee);
    }

    @Test
    public void testEndNewline() {
        String testee = Pyprnt.prnt(new String[]{"010", "8282", "8282"}, true, true, 50);
        String expect = "010 8282 8282";
        assertEquals(expect, testee);
    }
    
    @Test
    public void testEndDash() {
        String testee = Pyprnt.prnt(new String[]{"010", "8282", "8282"}, "--", true, true, 50);
        String expect = "010 8282 8282--";
        assertEquals(expect, testee);
    }
}