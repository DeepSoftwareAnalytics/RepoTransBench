import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestSegment {

    private Segment segment;
    private Segment otherSegment;

    @Before
    public void setUp() {
        segment = new Segment(1.0, 9.0);
    }

    @Test
    public void testCreation() {
        Segment segment = new Segment(1.0, 9.0);
        assertEquals("[ 00:00:01.000 -->  00:00:09.000]", segment.toString());
        assertEquals(9.0, segment.getEnd(), 0.001);
        assertEquals(8.0, segment.getDuration(), 0.001);
        assertEquals(5.0, segment.getMiddle(), 0.001);
    }

    @Test
    public void testIntersection() {
        Segment otherSegment = new Segment(4.0, 13.0);
        assertTrue(segment.intersects(otherSegment));
        assertEquals(new Segment(4.0, 9.0), segment.intersection(otherSegment));

        // Test no intersection case
        otherSegment = new Segment(13.0, 20.0);
        assertFalse(segment.intersects(otherSegment));
    }

    @Test
    public void testInclusion() {
        Segment otherSegment = new Segment(5.0, 9.0);
        assertTrue(segment.includes(otherSegment));
        assertFalse(segment.overlaps(new Segment(23.0, 24.0)));
    }

    @Test
    public void testOtherOperations() {
        Segment otherSegment = new Segment(10.0, 30.0);
        // Union operation
        assertEquals(new Segment(1.0, 30.0), segment.union(otherSegment));

        // Exclusive OR operation (Symmetric Difference)
        otherSegment = new Segment(14.0, 15.0);
        assertEquals(new Segment(9.0, 14.0), segment.symmetricDifference(otherSegment));
    }

    @Test
    public void testSegmentPrecisionMode() {
        // Setting the precision to null
        Segment.setPrecision(null);
        assertNotEquals(
            new Segment(90 / 1000.0, (90 + 240) / 1000.0),
            new Segment(90 / 1000.0, (90 + 240 + 90) / 1000.0)
        );

        // Setting precision to 4 decimal places
        Segment.setPrecision(4);
        assertEquals(
            new Segment(90 / 1000.0, (90 + 240) / 1000.0),
            new Segment(90 / 1000.0, (90 + 240 + 90) / 1000.0)
        );
    }
}
