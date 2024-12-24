import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestCuid {

    private CuidGenerator generator;

    @BeforeEach
    public void setUp() {
        generator = new CuidGenerator();
    }

    @Test
    public void testModuleWorks() {
        String fullCuid = Cuid.cuid();
        assertEquals(25, fullCuid.length());
        String slug = Cuid.slug();
        assertEquals(7, slug.length());
    }

    @Test
    public void testGetProcessFingerprint() {
        String first = Cuid.getProcessFingerprint();
        String second = Cuid.getProcessFingerprint();
        assertEquals(4, first.length());
        assertEquals(first, second);
    }

    @Test
    public void testSafeCounter() {
        int val = generator.getCounter();
        int val2 = generator.getCounter();
        assertTrue(val2 > val);
        generator.setCounter((int)Cuid.DISCRETE_VALUES - 1);
        int val3 = generator.getCounter();
        assertEquals(0, val3);
    }

    @Test
    public void testGeneratesString() {
        assertTrue(generator.cuid() instanceof String);
    }

    @Test
    public void testFormatMatches() {
        String ident = generator.cuid();
        assertEquals(25, ident.length());
        assertEquals('c', ident.charAt(0));
    }

    @Test
    public void testNoCollisions() {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 99999; i++) {
            seen.add(generator.cuid());
        }
        assertEquals(99999, seen.size());
    }

    @Test
    public void testFewCollisionsWithSlug() {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 5000; i++) {
            seen.add(generator.slug());
        }
        assertTrue(5000 - seen.size() < 50);
    }

    @Test
    public void testSequential() {
        String previous = generator.cuid();
        for (int i = 0; i < 99999; i++) {
            String current = generator.cuid();
            assertTrue(previous.compareTo(current) < 0);
            previous = current;
        }
    }

    @Test
    public void testIsFast() {
        long startTime = System.nanoTime();
        for (int i = 0; i < 9999; i++) {
            generator.cuid();
        }
        long endTime = System.nanoTime();
        double timePerCuid = (endTime - startTime) / 9999.0;
        assertTrue(timePerCuid < 200000); // 0.0002 seconds = 200,000 nanoseconds
    }
}
