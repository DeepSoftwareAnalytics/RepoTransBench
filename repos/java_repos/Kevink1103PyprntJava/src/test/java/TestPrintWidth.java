import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import pyprnt.Pyprnt;

public class TestPrintWidth {

    @Test
    public void testWidthMinimum20() {
        String[] input = {"Kevin Kim is a developer."};
        String testee = Pyprnt.prnt(input, true, true, 20);
        String expect = "┌─┬────────────────┐\n│0│Kevin Kim is a d│\n│ │eveloper.       │\n└─┴────────────────┘";
        assertEquals(expect, testee);
    }
}
