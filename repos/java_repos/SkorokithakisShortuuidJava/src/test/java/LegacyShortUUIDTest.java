import org.junit.jupiter.api.Test;
import shortuuid.ShortUUID;

import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class LegacyShortUUIDTest {

    @Test
    void testGeneration() {
        ShortUUID su = new ShortUUID();
        assertTrue(su.uuid().length() > 20 && su.uuid().length() < 24);
        assertTrue(su.uuid("http://www.example.com/").length() > 20 && su.uuid("http://www.example.com/").length() < 24);
        assertTrue(su.uuid("HTTP://www.example.com/").length() > 20 && su.uuid("HTTP://www.example.com/").length() < 24);
        assertTrue(su.uuid("example.com/").length() > 20 && su.uuid("example.com/").length() < 24);
    }

    @Test
    void testEncoding() {
        ShortUUID su = new ShortUUID();
        UUID u = UUID.fromString("3b1f8b40-222c-4a6e-b77e-779d5a94e21c");
        assertEquals("CXc85b4rqinB7s5J52TRYb", su.encode(u));
    }

    @Test
    void testDecoding() {
        ShortUUID su = new ShortUUID();
        UUID u = UUID.fromString("3b1f8b40-222c-4a6e-b77e-779d5a94e21c");
        assertEquals(u, su.decode("CXc85b4rqinB7s5J52TRYb"));
    }

    @Test
    void testAlphabet() {
        ShortUUID su = new ShortUUID();
        String backupAlphabet = su.getAlphabet();

        String alphabet = "01";
        su.setAlphabet(alphabet);
        assertEquals(alphabet, su.getAlphabet());

        su.setAlphabet("01010101010101");
        assertEquals(alphabet, su.getAlphabet());

        assertEquals(Set.of("0", "1"), new HashSet<>(Arrays.asList(su.uuid().split(""))));
        assertTrue(su.uuid().length() > 116 && su.uuid().length() < 140);

        UUID u = UUID.randomUUID();
        assertEquals(u, su.decode(su.encode(u)));

        u = UUID.fromString(su.uuid());
        assertEquals(u, su.decode(su.encode(u)));

        assertThrows(IllegalArgumentException.class, () -> su.setAlphabet("1"));
        assertThrows(IllegalArgumentException.class, () -> su.setAlphabet("1111111"));

        su.setAlphabet(backupAlphabet);

        assertThrows(IllegalArgumentException.class, () -> new ShortUUID("0"));
    }

    @Test
    void testRandom() {
        ShortUUID su = new ShortUUID();
        assertEquals(22, su.random().length());
        for (int i = 1; i < 100; i++) {
            assertEquals(i, su.random(i).length());
        }
    }
}
