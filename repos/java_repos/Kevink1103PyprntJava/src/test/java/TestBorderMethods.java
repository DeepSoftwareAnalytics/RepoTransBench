import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import pyprnt.util.Util;

public class TestBorderMethods {

    @Test
    public void testTopBorder() {
        String testee = Util.border("top", 5, 5, 20);
        String expect = "┌─────┬─────┐";
        assertEquals(expect, testee);
    }
    
    @Test
    public void testBottomBorder() {
        String testee = Util.border("bottom", 5, 5, 20);
        String expect = "└─────┴─────┘";
        assertEquals(expect, testee);
    }
    
    @Test
    public void testExceedBorder() {
        String testee1 = Util.border("top", 5, 20, 20);
        int testee2 = testee1.length();
        String expect1 = "┌─────┬────────────┐";
        int expect2 = 20;
        assertEquals(expect1, testee1);
        assertEquals(expect2, testee2);
    }
}
