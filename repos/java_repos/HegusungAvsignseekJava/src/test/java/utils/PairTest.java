package utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PairTest {

    @Test
    public void testPair() {
        Pair<Integer, String> pair = new Pair<>(1, "value");
        assertEquals(1, pair.getKey());
        assertEquals("value", pair.getValue());
    }
}
