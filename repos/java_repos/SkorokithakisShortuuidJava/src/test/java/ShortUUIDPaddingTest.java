import org.junit.jupiter.api.Test;
import shortuuid.ShortUUID;

import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class ShortUUIDPaddingTest {

    @Test
    void testPadding() {
        ShortUUID su = new ShortUUID();
        UUID randomUid = UUID.randomUUID();
        UUID smallestUid = new UUID(0, 0);

        String encodedRandom = su.encode(randomUid);
        String encodedSmall = su.encode(smallestUid);

        assertEquals(encodedRandom.length(), encodedSmall.length());
    }

    @Test
    void testDecoding() {
        ShortUUID su = new ShortUUID();
        UUID randomUid = UUID.randomUUID();
        UUID smallestUid = new UUID(0, 0);

        String encodedRandom = su.encode(randomUid);
        String encodedSmall = su.encode(smallestUid);

        assertEquals(su.decode(encodedSmall), smallestUid);
        assertEquals(su.decode(encodedRandom), randomUid);
    }

    @Test
    void testConsistency() {
        ShortUUID su = new ShortUUID();
        int numIterations = 1000;
        Map<Integer, Integer> uidLengthCounts = new HashMap<>();

        for (int i = 0; i < numIterations; i++) {
            UUID randomUid = UUID.randomUUID();
            String encodedRandom = su.encode(randomUid);
            int length = encodedRandom.length();
            uidLengthCounts.put(length, uidLengthCounts.getOrDefault(length, 0) + 1);
            UUID decodedRandom = su.decode(encodedRandom);

            assertEquals(randomUid, decodedRandom);
        }

        assertEquals(1, uidLengthCounts.size());
        int uidLength = uidLengthCounts.keySet().iterator().next();
        assertEquals(numIterations, (int) uidLengthCounts.values().iterator().next());
    }
}
