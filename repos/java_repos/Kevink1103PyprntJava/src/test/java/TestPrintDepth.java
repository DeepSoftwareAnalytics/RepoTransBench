import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import pyprnt.Pyprnt;

public class TestPrintDepth {

    @Test
    public void testDepthInfinityBasic() {
        Object[] input = {new Object[]{new Object[]{new Object[]{}}}, new Object[]{}};
        String testee = Pyprnt.prnt(input, true, true, 50);
        String expect = "┌─┬──────┐\n│0│┌─┬──┐│\n│ ││0│[]││\n│ │└─┴──┘│\n│1│[]    │\n└─┴──────┘";
        assertEquals(expect, testee);
    }

    @Test
    public void testDepthInfinityComplex() {
        Object[] input = {new Object[]{new Object[]{new Object[]{new Object()}}}, new Object[]{}};
        String testee = Pyprnt.prnt(input, true, true, 50);
        String expect = "┌─┬──────────┐\n│0│┌─┬──────┐│\n│ ││0│┌─┬──┐││\n│ ││ ││0│{}││\n│ ││ │└─┴──┘││\n│ │└─┴──────┘│\n│1│[]        │\n└─┴──────────┘";
        assertEquals(expect, testee);
    }

    @Test
    public void testDepth1Basic() {
        Object[] input = {new Object[]{new Object[]{new Object[]{}}}, new Object[]{}};
        String testee = Pyprnt.prnt(input, 1, true, 50);
        String expect = "┌─┬────┐\n│0│[[]]│\n│1│[]  │\n└─┴────┘";
        assertEquals(expect, testee);
    }

    @Test
    public void testDepth1Complex() {
        Object[] input = {new Object[]{new Object[]{new Object[]{new Object()}}}, new Object[]{}};
        String testee = Pyprnt.prnt(input, 1, true, 50);
        String expect = "┌─┬──────┐\n│0│[[{}]]│\n│1│[]    │\n└─┴──────┘";
        assertEquals(expect, testee);
    }

    @Test
    public void testDepth2Basic() {
        Object[] input = {new Object[]{new Object[]{new Object[]{}}}, new Object[]{}};
        String testee = Pyprnt.prnt(input, 2, true, 50);
        String expect = "┌─┬──────┐\n│0│┌─┬──┐│\n│ ││0│[]││\n│ │└─┴──┘│\n│1│[]    │\n└─┴──────┘";
        assertEquals(expect, testee);
    }

    @Test
    public void testDepth2Complex() {
        Object[] input = {new Object[]{new Object[]{new Object[]{new Object()}}}, new Object[]{}};
        String testee = Pyprnt.prnt(input, 2, true, 50);
        String expect = "┌─┬────────┐\n│0│┌─┬────┐│\n│ ││0│[{}]││\n│ │└─┴────┘│\n│1│[]      │\n└─┴────────┘";
        assertEquals(expect, testee);
    }
}
