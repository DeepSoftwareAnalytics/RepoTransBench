import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import pyprnt.Pyprnt;

public class TestPrintEnable {

    @Test
    public void testEnableFalse() {
        String[] creation = {"Adam", "Eve"};
        String testee = Pyprnt.prnt(creation, false, true, 50);
        String expect = "['Adam', 'Eve']";
        assertEquals(expect, testee);
    }
}
