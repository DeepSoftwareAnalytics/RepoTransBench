import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import pyprnt.util.Util;

public class TestTerminalMethods {

    @Test
    public void testGetTerminalSize() {
        int testee = Util.getTerminalSize();
        int expect = 1;
        assertTrue(testee > expect);
    }
}
