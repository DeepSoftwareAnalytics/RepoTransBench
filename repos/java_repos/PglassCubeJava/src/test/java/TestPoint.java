import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestPoint {
    @Test
    public void testPointConstructor() {
        Point p = new Point(1, 2, 3);
        assertEquals(1, p.x);
        assertEquals(2, p.y);
        assertEquals(3, p.z);
    }

    @Test
    public void testPointCount() {
        Point p = new Point(2, 2, 3);
        assertEquals(2, p.count(2));
        assertEquals(1, p.count(3));
        assertEquals(0, p.count(5));
        assertEquals(3, new Point(9, 9, 9).count(9));
    }

    @Test
    public void testPointEq() {
        Point p = new Point(1, 2, 3);
        Point pp = new Point(1, 2, 3);
        assertEquals(p, pp);
        assertTrue(p.equals(pp));
    }

    @Test
    public void testPointNeq() {
        Point p1 = new Point(1, 2, 3);
        Point p2 = new Point(1, 2, 4);
        assertNotEquals(p1, p2);
    }

    @Test
    public void testPointAdd() {
        Point p = new Point(1, 2, 3);
        Point q = new Point(2, 5, 9);
        assertEquals(new Point(3, 7, 12), p.add(q));
    }

    @Test
    public void testPointSub() {
        Point p = new Point(1, 2, 3);
        Point q = new Point(2, 5, 9);
        assertEquals(new Point(-1, -3, -6), p.subtract(q));
    }

    @Test
    public void testPointScale() {
        Point p = new Point(1, 2, 3);
        assertEquals(new Point(3, 6, 9), p.scale(3));
    }

    @Test
    public void testPointDotProduct() {
        Point p = new Point(1, 2, 3);
        Point q = new Point(2, 5, 9);
        assertEquals(39, p.dot(q));
    }

    @Test
    public void testPointCrossProduct() {
        Point p = new Point(1, 2, 3);
        Point q = new Point(2, 5, 9);
        assertEquals(new Point(3, -3, 1), p.cross(q));
    }
}
