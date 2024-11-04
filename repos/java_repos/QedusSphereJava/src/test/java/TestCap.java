import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class TestCap {

    private static final double EPS = 1e-14;
    private static final double DEGREE_EPS = 1e-13;

    @Before
    public void setUp() {
        // 初始化操作，如果需要的话可以在这里进行
    }

    private Point getLatLonPoint(double latDegrees, double lonDegrees) {
        return LatLon.fromDegrees(latDegrees, lonDegrees).toPoint();
    }

    @Test
    public void testBasic() {
        Cap empty = Cap.empty();
        Cap full = Cap.full();

        assertTrue(empty.isValid());
        assertTrue(empty.isEmpty());
        assertTrue(empty.complement().isFull());

        assertTrue(full.isValid());
        assertTrue(full.isFull());
        assertTrue(full.complement().isEmpty());
        assertEquals(2, full.getHeight(), 0.000001);
        assertEquals(180.0, full.getAngle().degrees(), 0.000001);

        Cap defaultEmpty = new Cap();
        assertTrue(defaultEmpty.isValid());
        assertTrue(defaultEmpty.isEmpty());
        assertEquals(empty.getAxis(), defaultEmpty.getAxis());
        assertEquals(empty.getHeight(), defaultEmpty.getHeight(), 0.000001);

        // Containment and intersection of empty and full caps
        assertTrue(empty.contains(empty));
        assertTrue(full.contains(empty));
        assertTrue(full.contains(full));
        assertFalse(empty.interiorIntersects(empty));
        assertTrue(full.interiorIntersects(full));
        assertFalse(full.interiorIntersects(empty));

        // Singleton cap containing the x-axis
        Cap xaxis = Cap.fromAxisHeight(new Point(1, 0, 0), 0);
        assertTrue(xaxis.contains(new Point(1, 0, 0)));
        assertFalse(xaxis.contains(new Point(1, 1e-20, 0)));
        assertEquals(0, xaxis.getAngle().radians(), 0.000001);

        // Singleton cap containing the y-axis
        Cap yaxis = Cap.fromAxisAngle(new Point(0, 1, 0), Angle.fromRadians(0));
        assertFalse(yaxis.contains(xaxis.getAxis()));
        assertEquals(0, xaxis.getHeight(), 0.000001);

        // Check that the complement of a singleton cap is the full cap
        Cap xcomp = xaxis.complement();
        assertTrue(xcomp.isValid());
        assertTrue(xcomp.isFull());
        assertTrue(xcomp.contains(xaxis.getAxis()));

        // Check that the complement of the complement is *not* the original
        assertTrue(xcomp.complement().isValid());
        assertTrue(xcomp.complement().isEmpty());
        assertFalse(xcomp.complement().contains(xaxis.getAxis()));

        // Check that very small caps can be represented accurately
        double kTinyRad = 1e-10;
        Cap tiny = Cap.fromAxisAngle(new Point(1, 2, 3).normalize(),
                                     Angle.fromRadians(kTinyRad));
        Point tangent = tiny.getAxis().crossProd(new Point(3, 2, 1)).normalize();
        assertTrue(tiny.contains(tiny.getAxis().add(tangent.mul(0.99 * kTinyRad))));
        assertFalse(tiny.contains(tiny.getAxis().add(tangent.mul(1.01 * kTinyRad))));

        // Basic tests on a hemispherical cap
        Cap hemi = Cap.fromAxisHeight(new Point(1, 0, 1).normalize(), 1);
        assertEquals(hemi.getAxis().negate(), hemi.complement().getAxis());
        assertEquals(1, hemi.complement().getHeight(), 0.000001);
        assertTrue(hemi.contains(new Point(1, 0, 0)));
        assertFalse(hemi.complement().contains(new Point(1, 0, 0)));
        assertTrue(hemi.contains(new Point(1, 0, -(1 - EPS)).normalize()));
        assertFalse(hemi.interiorContains(new Point(1, 0, -(1 + EPS)).normalize()));

        // A concave cap
        Cap concave = Cap.fromAxisAngle(getLatLonPoint(80, 10), Angle.fromDegrees(150));
        assertTrue(concave.contains(getLatLonPoint(-70 * (1 - EPS), 10)));
        assertFalse(concave.contains(getLatLonPoint(-70 * (1 + EPS), 10)));
        assertTrue(concave.contains(getLatLonPoint(-50 * (1 - EPS), -170)));
        assertFalse(concave.contains(getLatLonPoint(-50 * (1 + EPS), -170)));

        // Cap containment tests
        assertFalse(empty.contains(xaxis));
        assertFalse(empty.interiorIntersects(xaxis));
        assertTrue(full.contains(xaxis));
        assertTrue(full.interiorIntersects(xaxis));
        assertFalse(xaxis.contains(full));
        assertFalse(xaxis.interiorIntersects(full));
        assertTrue(xaxis.contains(xaxis));
        assertFalse(xaxis.interiorIntersects(xaxis));
        assertTrue(xaxis.contains(empty));
        assertFalse(xaxis.interiorIntersects(empty));
        assertTrue(hemi.contains(tiny));
        assertTrue(hemi.contains(Cap.fromAxisAngle(new Point(1, 0, 0),
                                                   Angle.fromRadians(Math.PI / 4.0 - EPS))));
        assertFalse(hemi.contains(Cap.fromAxisAngle(new Point(1, 0, 0),
                                                    Angle.fromRadians(Math.PI / 4.0 + EPS))));
        assertTrue(concave.contains(hemi));
        assertTrue(concave.interiorIntersects(hemi.complement()));
        assertFalse(concave.contains(Cap.fromAxisHeight(concave.getAxis().negate(), 0.1)));
    }

    private static final double EPS = 1e-14;
    private static final double DEGREE_EPS = 1e-13;

    private Point getLatLonPoint(double latDegrees, double lonDegrees) {
        return LatLon.fromDegrees(latDegrees, lonDegrees).toPoint();
    }

    @Test
    public void testGetRectBound() {
        // Empty and full caps
        assertTrue(Cap.empty().getRectBound().isEmpty());
        assertTrue(Cap.full().getRectBound().isFull());

        // Cap that includes the south pole
        S2LatLonRect rect = Cap.fromAxisAngle(getLatLonPoint(-45, 57), Angle.fromDegrees(50)).getRectBound();
        assertEquals(-90, rect.latLo().degrees(), DEGREE_EPS);
        assertEquals(5, rect.latHi().degrees(), DEGREE_EPS);
        assertTrue(rect.lon().isFull());

        // Cap that is tangent to the north pole
        rect = Cap.fromAxisAngle(new Point(1, 0, 1).normalize(), Angle.fromRadians(Math.PI / 4.0 + 1e-16)).getRectBound();
        assertEquals(0, rect.lat().lo(), EPS);
        assertEquals(Math.PI / 2.0, rect.lat().hi(), EPS);
        assertTrue(rect.lon().isFull());

        rect = Cap.fromAxisAngle(new Point(1, 0, 1).normalize(), Angle.fromDegrees(45 + 5e-15)).getRectBound();
        assertEquals(0, rect.latLo().degrees(), DEGREE_EPS);
        assertEquals(90, rect.latHi().degrees(), DEGREE_EPS);
        assertTrue(rect.lon().isFull());

        // The eastern hemisphere
        rect = Cap.fromAxisAngle(new Point(0, 1, 0), Angle.fromRadians(Math.PI / 2.0 + 2e-16)).getRectBound();
        assertEquals(-90, rect.latLo().degrees(), DEGREE_EPS);
        assertEquals(90, rect.latHi().degrees(), DEGREE_EPS);
        assertTrue(rect.lon().isFull());

        // A cap centered on the equator
        rect = Cap.fromAxisAngle(getLatLonPoint(0, 50), Angle.fromDegrees(20)).getRectBound();
        assertEquals(-20, rect.latLo().degrees(), DEGREE_EPS);
        assertEquals(20, rect.latHi().degrees(), DEGREE_EPS);
        assertEquals(30, rect.lonLo().degrees(), DEGREE_EPS);
        assertEquals(70, rect.lonHi().degrees(), DEGREE_EPS);

        // A cap centered on the north pole
        rect = Cap.fromAxisAngle(getLatLonPoint(90, 123), Angle.fromDegrees(10)).getRectBound();
        assertEquals(80, rect.latLo().degrees(), DEGREE_EPS);
        assertEquals(90, rect.latHi().degrees(), DEGREE_EPS);
        assertTrue(rect.lon().isFull());
    }

    @Test
    public void testCellMethods() {
        double faceRadius = Math.atan(Math.sqrt(2));

        for (int face = 0; face < 6; face++) {
            // The cell consisting of the entire face
            Cell rootCell = Cell.fromFacePosLevel(face, 0, 0);

            // A leaf cell at the midpoint of the v=1 edge
            Cell edgeCell = Cell.fromPoint(S2Sphere.faceUvToXyz(face, 0, 1 - EPS));

            // A leaf cell at the u=1, v=1 corner
            Cell cornerCell = Cell.fromPoint(S2Sphere.faceUvToXyz(face, 1 - EPS, 1 - EPS));

            // Quick check for full and empty caps
            assertTrue(Cap.full().contains(rootCell));
            assertFalse(Cap.empty().mayIntersect(rootCell));

            // Check intersections with the bounding caps of the leaf cells
            CellId first = cornerCell.id().advance(-3);
            CellId last = cornerCell.id().advance(4);
            CellId id = first;

            while (id.lessThan(last)) {
                Cell cell = new Cell(id);
                assertEquals(id.equals(cornerCell.id()), cell.getCapBound().contains(cornerCell));
                assertEquals(id.parent().contains(cornerCell.id()), cell.getCapBound().mayIntersect(cornerCell));
                id = id.next();
            }

            int antiFace = (face + 3) % 6;  // Opposite face
            for (int capFace = 0; capFace < 6; capFace++) {
                Point center = S2Sphere.getNorm(capFace);
                Cap covering = Cap.fromAxisAngle(center, Angle.fromRadians(faceRadius + EPS));

                assertEquals(capFace == face, covering.contains(rootCell));
                assertEquals(capFace != antiFace, covering.mayIntersect(rootCell));
                assertEquals(center.dotProd(edgeCell.getCenter()) > 0.1, covering.contains(edgeCell));
                assertEquals(covering.mayIntersect(edgeCell), covering.contains(edgeCell));
                assertEquals(capFace == face, covering.contains(cornerCell));
                assertEquals(center.dotProd(cornerCell.getCenter()) > 0, covering.mayIntersect(cornerCell));

                Cap bulging = Cap.fromAxisAngle(center, Angle.fromRadians(Math.PI / 4.0 + EPS));
                assertFalse(bulging.contains(rootCell));
                assertEquals(capFace != antiFace, bulging.mayIntersect(rootCell));
                assertEquals(capFace == face, bulging.contains(edgeCell));
                assertEquals(center.dotProd(edgeCell.getCenter()) > 0.1, bulging.mayIntersect(edgeCell));
                assertFalse(bulging.contains(cornerCell));
                assertFalse(bulging.mayIntersect(cornerCell));

                Cap singleton = Cap.fromAxisAngle(center, Angle.fromRadians(0));
                assertEquals(capFace == face, singleton.mayIntersect(rootCell));
                assertFalse(singleton.mayIntersect(edgeCell));
                assertFalse(singleton.mayIntersect(cornerCell));
            }
        }
    }

    @Test
    public void testExpanded() {
        assertTrue(Cap.empty().expanded(Angle.fromRadians(2)).isEmpty());
        assertTrue(Cap.full().expanded(Angle.fromRadians(2)).isFull());

        Cap cap50 = Cap.fromAxisAngle(new Point(1, 0, 0), Angle.fromDegrees(50));
        Cap cap51 = Cap.fromAxisAngle(new Point(1, 0, 0), Angle.fromDegrees(51));

        assertTrue(cap50.expanded(Angle.fromRadians(0)).approxEquals(cap50));
        assertTrue(cap50.expanded(Angle.fromDegrees(1)).approxEquals(cap51));
        assertFalse(cap50.expanded(Angle.fromDegrees(129.99)).isFull());
        assertTrue(cap50.expanded(Angle.fromDegrees(130.01)).isFull());
    }
}
