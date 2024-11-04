import org.junit.jupiter.api.Test;

import java.time.LocalDateTime; // Missing import
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class TestKsuidSerializationSupport {

    @Test
    public void testConvertsToBase62AndReturnsValueWithNumbersAndLetters() {
        Ksuid uid = new Ksuid();
        String value = uid.toBase62();
        assertNotNull(value);
        assertFalse(value.isEmpty());

        for (char v : value.toCharArray()) {
            assertTrue(Character.isLetterOrDigit(v));
        }
    }

    @Test
    public void testSuccessfullyConvertsToBase62AndViceVersa() {
        Ksuid uid1 = new Ksuid();
        String serialized = uid1.toBase62();

        Ksuid uid2 = Ksuid.fromBase62(serialized);

        assertEquals(uid1.toString(), uid2.toString());
        assertArrayEquals(uid1.toBytes(), uid2.toBytes());
        assertEquals(uid1.toBase62(), uid2.toBase62());
    }

    @Test
    public void testSuccessfullyConvertsToBytesAndViceVersa() {
        Ksuid uid1 = new Ksuid();
        byte[] serialized = uid1.toBytes();

        Ksuid uid2 = Ksuid.fromBytes(serialized);

        assertEquals(uid1.toString(), uid2.toString());
        assertArrayEquals(uid1.toBytes(), uid2.toBytes());
        assertEquals(uid1.toBase62(), uid2.toBase62());
    }

    @Test
    public void testBase62Orderable() {
        List<Ksuid> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            list.add(new Ksuid());
        }

        list.sort((x, y) -> y.toBase62().compareTo(x.toBase62()));

        for (int i = 0; i < list.size() - 1; i++) {
            assertTrue(list.get(i).getTimestamp() >= list.get(i + 1).getTimestamp());
        }
    }

    @Test
    public void testMeasure1sAndCompareKsuid() throws InterruptedException {
        final int DELAY_INTERVAL_SECS = 1;

        Ksuid u1 = new Ksuid();
        TimeUnit.SECONDS.sleep(DELAY_INTERVAL_SECS);
        Ksuid u2 = new Ksuid();

        assertTrue(u1.toBase62().compareTo(u2.toBase62()) < 0);
        assertTrue(u2.getTimestamp() > u1.getTimestamp());
        assertTrue(u2.getTimestamp() - u1.getTimestamp() >= 1);

        LocalDateTime d2 = u2.getDatetime();
        LocalDateTime d1 = u1.getDatetime();
        assertTrue(d2.isAfter(d1));
        assertTrue(d2.minusSeconds(1).isAfter(d1));
        assertArrayEquals(u1.getPayload(), u2.getPayload(), "Payload should not be equal");
    }

    @Test
    public void testIntegrationTest() {
        Ksuid uid1 = new Ksuid();
        byte[] bu = uid1.toBytes();

        assertNotNull(bu);

        Ksuid uid2 = Ksuid.fromBytes(bu);

        assertEquals(uid1.toString(), uid2.toString());
        assertArrayEquals(uid1.toBytes(), uid2.toBytes());
        assertEquals(uid1.toBase62(), uid2.toBase62());

        String b62 = uid1.toBase62();
        Ksuid uid3 = Ksuid.fromBase62(b62);

        assertEquals(uid1.toString(), uid3.toString());
        assertArrayEquals(uid1.toBytes(), uid2.toBytes());
        assertArrayEquals(uid1.toBytes(), uid3.toBytes());
        assertEquals(uid1.toBase62(), uid3.toBase62());

        byte[] bs = uid1.toBytes();
        assertNotNull(bs);
    }

    @Test
    public void testBulkTestForBase62WithDelays() throws InterruptedException {
        List<Ksuid> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(new Ksuid());
            TimeUnit.MILLISECONDS.sleep(10);
        }

        List<Ksuid> sortedList = new ArrayList<>(list);
        sortedList.sort((x, y) -> y.toBase62().compareTo(x.toBase62()));

        for (int i = 0; i < list.size() - 1; i++) {
            assertTrue(sortedList.get(i).getTimestamp() >= sortedList.get(i + 1).getTimestamp());
        }
    }
}
