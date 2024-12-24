import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import pyprnt.Pyprnt;

public class TestPrintBoth {

    @Test
    public void testBothTrue() {
        String[] creation = {"Adam", "Eve"};
        String testee = Pyprnt.prnt(creation, true, true, 50);
        String expect = "['Adam', 'Eve']\n┌─┬────┐\n│0│Adam│\n│1│Eve │\n└─┴────┘";
        assertEquals(expect, testee);
    }
}
