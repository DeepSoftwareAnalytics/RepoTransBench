import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Arrays;

public class TestSlidingWindowFeature {

    private SlidingWindowFeature features;
    private Segment segment;

    @Before
    public void setUp() {
        // 初始化 features，相当于 Python 中的 features fixture
        double[][] data = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
            {10, 11, 12, 13, 14, 15, 16, 17, 18, 19}
        };
        SlidingWindow window = new SlidingWindow(0.0, 1.0, 2.0);
        features = new SlidingWindowFeature(data, window);

        // 初始化 segment，相当于 Python 中的 segment fixture
        segment = new Segment(3.3, 6.7);
    }

    @Test
    public void testCropLoose() {
        double[][] actual = features.crop(segment, "loose");
        double[][] expected = {
            {2, 3, 4, 5, 6},
            {12, 13, 14, 15, 16}
        };
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testCropStrict() {
        double[][] actual = features.crop(segment, "strict");
        double[][] expected = {
            {4},
            {14}
        };
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testCropCenter() {
        double[][] actual = features.crop(segment, "center");
        double[][] expected = {
            {2, 3, 4, 5, 6},
            {12, 13, 14, 15, 16}
        };
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testCropFixed() {
        double[][] actual = features.crop(segment, "center", 4.0);
        double[][] expected = {
            {2, 3, 4, 5},
            {12, 13, 14, 15}
        };
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testCropOutOfBounds() {
        Segment outOfBoundsSegment = new Segment(-6.0, -1.0);
        double[][] actual = features.crop(outOfBoundsSegment, "strict");
        double[][] expected = new double[0][2]; // Empty array
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testCropFixedOutOfBounds() {
        Segment outOfBoundsSegment = new Segment(-2.0, 6.7);
        double[][] actual = features.crop(outOfBoundsSegment, "center", 8.7);
        double[][] expected = {
            {0, 0, 0, 0, 1, 2, 3, 4, 5},
            {10, 10, 10, 10, 11, 12, 13, 14, 15}
        };
        assertArrayEquals(expected, actual);
    }

    // Helper method for comparing 2D arrays
    private void assertArrayEquals(double[][] expected, double[][] actual) {
        assertEquals("Array row count mismatch", expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals("Array element mismatch at row " + i, expected[i], actual[i], 0.001);
        }
    }
}
