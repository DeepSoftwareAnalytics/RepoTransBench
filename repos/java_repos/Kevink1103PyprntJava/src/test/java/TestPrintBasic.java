import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import pyprnt.Pyprnt;

public class TestPrintBasic {

    @Test
    public void testListBasic() {
        String[] creation = {"Adam", "Eve"};
        String testee = Pyprnt.prnt(creation, true, true, 50);
        String expect = "┌─┬────┐\n│0│Adam│\n│1│Eve │\n└─┴────┘";
        assertEquals(expect, testee);
    }

    @Test
    public void testDictBasic() {
        Map<String, Object> menu = new LinkedHashMap<>();
        menu.put("kimchi", 5000);
        menu.put("Ice Cream", 100);
        String testee = Pyprnt.prnt(menu, true, true, 50);
        String expect = "┌─────────┬────┐\n│kimchi   │5000│\n│Ice Cream│100 │\n└─────────┴────┘";
        assertEquals(expect, testee);
    }

    @Test
    public void testListWithNewline() {
        String[] creation = {"Ad\nam", "Eve"};
        String testee = Pyprnt.prnt(creation, true, true, 50);
        String expect = "┌─┬──────┐\n│0│Ad\\nam│\n│1│Eve   │\n└─┴──────┘";
        assertEquals(expect, testee);
    }

    @Test
    public void testDictWithNewline() {
        Map<String, Object> menu = new LinkedHashMap<>();
        menu.put("kimchi", 5000);
        menu.put("Ice\nCream", "1 €\n1.08 $");
        String testee = Pyprnt.prnt(menu, true, true, 50);
        String expect = "┌──────────┬───────────┐\n│kimchi    │5000       │\n│Ice\\nCream│1 €\\n1.08 $│\n└──────────┴───────────┘";
        assertEquals(expect, testee);
    }
}
