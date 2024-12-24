import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class KsuidTest {
    private List<Ksuid> ksList;
    private Ksuid ksuid1;
    private Ksuid ksuid2;

    @BeforeEach
    public void setUp() {
        ksList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            ksList.add(new Ksuid());
        }
        ksuid1 = new Ksuid();
        ksuid2 = new Ksuid();
    }

    @Test
    public void testTimeStamp() {
        assertTrue(ksuid1.getTimestamp() <= ksuid2.getTimestamp());
        assertEquals(LocalDateTime.now().getDayOfMonth(), ksuid1.getDatetime().getDayOfMonth());
    }

    @Test
    public void testSort() {
        assertTrue(ksList.size() > 0);
        List<Ksuid> sortedList = Utils.sortKSUID(ksList);

        for (int i = 0; i < sortedList.size() - 1; i++) {
            assertTrue(sortedList.get(i).getTimestamp() <= sortedList.get(i + 1).getTimestamp());
        }
    }

    @Test
    public void testStringFunction() {
        for (Ksuid val : ksList) {
            assertEquals(val.toString(), val.toString());
        }
    }

    @Test
    public void testDifferentUIDs() {
        for (int i = 0; i < ksList.size(); i++) {
            for (int j = i + 1; j < ksList.size(); j++) {
                assertNotEquals(ksList.get(j).toString(), ksList.get(i).toString());
            }
        }
    }
}
