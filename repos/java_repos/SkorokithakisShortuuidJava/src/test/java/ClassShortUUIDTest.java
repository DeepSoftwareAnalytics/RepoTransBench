import org.junit.jupiter.api.Test;
import shortuuid.ShortUUID;

import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ClassShortUUIDTest {

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
    void testRandom() {
        ShortUUID su = new ShortUUID();
        for (int i = 0; i < 1000; i++) {
            assertEquals(22, su.random().length());
        }
        for (int i = 1; i < 100; i++) {
            assertEquals(i, su.random(i).length());
        }
    }

    @Test
    void testAlphabet() {
        String alphabet = "01";
        ShortUUID su1 = new ShortUUID(alphabet);
        ShortUUID su2 = new ShortUUID();

        assertEquals(alphabet, su1.getAlphabet());

        su1.setAlphabet("01010101010101");
        assertEquals(alphabet, su1.getAlphabet());

        assertEquals(Set.of("0", "1"), new HashSet<>(Arrays.asList(su1.uuid().split(""))));
        assertTrue(su1.uuid().length() > 116 && su1.uuid().length() < 140);
        assertTrue(su2.uuid().length() > 20 && su2.uuid().length() < 24);

        UUID u = UUID.randomUUID();
        assertEquals(u, su1.decode(su1.encode(u)));

        u = UUID.fromString(su1.uuid());
        assertEquals(u, su1.decode(su1.encode(u)));

        assertThrows(IllegalArgumentException.class, () -> su1.setAlphabet("1"));
        assertThrows(IllegalArgumentException.class, () -> su1.setAlphabet("1111111"));
    }

    @Test
    void testEncodedLength() {
        ShortUUID su1 = new ShortUUID();
        assertEquals(22, su1.encodedLength());

        String base64Alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
        ShortUUID su2 = new ShortUUID(base64Alphabet);
        assertEquals(22, su2.encodedLength());

        String binaryAlphabet = "01";
        ShortUUID su3 = new ShortUUID(binaryAlphabet);
        assertEquals(128, su3.encodedLength());

        ShortUUID su4 = new ShortUUID();
        assertEquals(11, su4.encodedLength(8));
    }
}
