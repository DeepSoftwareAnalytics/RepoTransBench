import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class TestCrossings {

    private int degen;

    @Before
    public void setUp() {
        degen = -2;
    }

    private void compareResult(int actual, int expected) {
        if (expected == degen) {
            assertTrue(actual <= 0);
        } else {
            assertEquals(expected, actual);
        }
    }

    private void checkCrossing(Point a, Point b, Point c, Point d, int robust, boolean edgeOrVertex, boolean simple) {
        a = a.normalize();
        b = b.normalize();
        c = c.normalize();
        d = d.normalize();

        // Use an assumed method similar to `s2sphere.simpleCrossing` to handle simple crossing.
        if (simple) {
            assertEquals(robust > 0, S2Sphere.simpleCrossing(a, b, c, d));
        }

        // Similar to S2EdgeUtil::EdgeOrVertexCrossing
        assertEquals(edgeOrVertex, S2EdgeUtil.edgeOrVertexCrossing(a, b, c, d));
    }

    private void checkCrossings(Point a, Point b, Point c, Point d, int robust, boolean edgeOrVertex, boolean simple) {
        checkCrossing(a, b, c, d, robust, edgeOrVertex, simple);
        checkCrossing(b, a, c, d, robust, edgeOrVertex, simple);
        checkCrossing(a, b, d, c, robust, edgeOrVertex, simple);
        checkCrossing(b, a, d, c, robust, edgeOrVertex, simple);
        checkCrossing(a, a, c, d, degen, false, false);
        checkCrossing(a, b, c, c, degen, false, false);
        checkCrossing(a, b, a, b, 0, true, false);
        checkCrossing(c, d, a, b, robust, edgeOrVertex ^ (robust == 0), simple);
    }

    @Test
    public void testCrossings() {
        // Simple crossings between two edges
        checkCrossings(new Point(1, 2, 1), new Point(1, -3, 0.5),
                new Point(1, -0.5, -3), new Point(0.1, 0.5, 3), 1, true, true);

        // Two edges crossing antipodal points
        checkCrossings(new Point(1, 2, 1), new Point(1, -3, 0.5),
                new Point(-1, 0.5, 3), new Point(-0.1, -0.5, -3), -1, false, true);

        // Two edges on the same great circle
        checkCrossings(new Point(0, 0, -1), new Point(0, 1, 0),
                new Point(0, 1, 1), new Point(0, 0, 1), -1, false, true);

        // Two edges that cross where one vertex is at the origin
        checkCrossings(new Point(1, 0, 0), S2Sphere.origin(),
                new Point(1, -0.1, 1), new Point(1, 1, -0.1), 1, true, true);

        // Two edges crossing antipodal points where one vertex is at the origin
        checkCrossings(new Point(1, 0, 0), new Point(0, 1, 0),
                new Point(0, 0, -1), new Point(-1, -1, 1), -1, false, true);

        // Two edges sharing an endpoint
        checkCrossings(new Point(2, 3, 4), new Point(-1, 2, 5),
                new Point(7, -2, 3), new Point(2, 3, 4), 0, false, true);

        // Two edges barely crossing near the middle of one edge
        checkCrossings(new Point(1, 1, 1), new Point(1, Double.MIN_VALUE, -1),
                new Point(11, -12, -1), new Point(10, 10, 1), 1, true, false);

        // Two edges separated by a distance of about 1e-15
        checkCrossings(new Point(1, 1, 1), new Point(1, Double.MIN_VALUE + 1, -1),
                new Point(1, -1, 0), new Point(1, 1, 0), -1, false, false);

        // Two edges barely crossing near the end of both edges
        checkCrossings(new Point(0, 0, 1), new Point(2, Double.MIN_VALUE, 1),
                new Point(1, -1, 1), new Point(Double.MIN_VALUE, 0, 1), 1, true, false);

        // Two edges separated by about 1e-640
        checkCrossings(new Point(0, 0, 1), new Point(2, Double.MIN_VALUE, 1),
                new Point(1, -1, 1), new Point(Double.MIN_VALUE, 0, 1), -1, false, false);

        // Two edges barely crossing near the middle of one edge, requiring high precision
        checkCrossings(new Point(1, Double.MIN_VALUE, -Double.MIN_VALUE),
                new Point(Double.MIN_VALUE, 1, Double.MIN_VALUE), 
                new Point(1, -1, Double.MIN_VALUE), new Point(1, 1, 0),
                1, true, false);

        // Two edges separated by a distance of about 1e-640, requiring high precision
        checkCrossings(new Point(1, Double.MIN_VALUE, -Double.MIN_VALUE),
                new Point(-Double.MIN_VALUE, 1, Double.MIN_VALUE), 
                new Point(1, -1, Double.MIN_VALUE), new Point(1, 1, 0),
                -1, false, false);
    }
}
