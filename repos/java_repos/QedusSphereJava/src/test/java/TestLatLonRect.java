import static org.junit.Assert.*;
import org.junit.Test;

public class TestLatLonRect {

    private LatLonRect rectFromDegrees(double latLo, double lonLo, double latHi, double lonHi) {
        return new LatLonRect(LatLon.fromDegrees(latLo, lonLo), LatLon.fromDegrees(latHi, lonHi));
    }

    @Test
    public void testEmptyAndFull() {
        LatLonRect empty = LatLonRect.empty();
        LatLonRect full = LatLonRect.full();
        assertTrue(empty.isValid());
        assertTrue(empty.isEmpty());
        assertFalse(empty.isPoint());
        assertTrue(full.isValid());
        assertTrue(full.isFull());
        assertFalse(full.isPoint());

        LatLonRect defaultEmpty = new LatLonRect();
        assertTrue(defaultEmpty.isValid());
        assertTrue(defaultEmpty.isEmpty());
        assertEquals(empty.lat().bounds(), defaultEmpty.lat().bounds());
        assertEquals(empty.lon().bounds(), defaultEmpty.lon().bounds());
    }

    @Test
    public void testAccessors() {
        LatLonRect d1 = rectFromDegrees(-90, 0, -45, 180);
        assertEquals(-90, d1.latLo().degrees(), 1e-9);
        assertEquals(-45, d1.latHi().degrees(), 1e-9);
        assertEquals(0, d1.lonLo().degrees(), 1e-9);
        assertEquals(180, d1.lonHi().degrees(), 1e-9);
        assertEquals(new LineInterval(-Math.PI / 2.0, -Math.PI / 4.0), d1.lat());
        assertEquals(new SphereInterval(0, Math.PI), d1.lon());
    }

    @Test
    public void testFromCenterSize() {
        assertTrue(LatLonRect.fromCenterSize(LatLon.fromDegrees(80, 170), LatLon.fromDegrees(40, 60))
                .approxEquals(rectFromDegrees(60, 140, 90, -160)));

        assertTrue(LatLonRect.fromCenterSize(LatLon.fromDegrees(10, 40), LatLon.fromDegrees(210, 400)).isFull());

        assertTrue(LatLonRect.fromCenterSize(LatLon.fromDegrees(-90, 180), LatLon.fromDegrees(20, 50))
                .approxEquals(rectFromDegrees(-90, 155, -80, -155)));
    }

    @Test
    public void testFromPoint() {
        LatLon p = LatLon.fromDegrees(23, 47);
        assertEquals(new LatLonRect(p, p), LatLonRect.fromPoint(p));
        assertTrue(LatLonRect.fromPoint(p).isPoint());
    }

    @Test
    public void testFromPointPair() {
        assertEquals(rectFromDegrees(-35, 155, 15, -140),
                LatLonRect.fromPointPair(LatLon.fromDegrees(-35, -140), LatLon.fromDegrees(15, 155)));
        assertEquals(rectFromDegrees(-90, -70, 25, 80),
                LatLonRect.fromPointPair(LatLon.fromDegrees(25, -70), LatLon.fromDegrees(-90, 80)));
    }

    @Test
    public void testGetCenterSize() {
        LatLonRect r1 = new LatLonRect(new LineInterval(0, Math.PI / 2.0), new SphereInterval(-Math.PI, 0));
        assertEquals(LatLon.fromRadians(Math.PI / 4.0, -Math.PI / 2.0), r1.getCenter());
        assertEquals(LatLon.fromRadians(Math.PI / 2.0, Math.PI), r1.getSize());
        assertTrue(LatLonRect.empty().getSize().lat().radians() < 0);
        assertTrue(LatLonRect.empty().getSize().lon().radians() < 0);
    }

    @Test
    public void testGetVertex() {
        LatLonRect r1 = new LatLonRect(new LineInterval(0, Math.PI / 2.0), new SphereInterval(-Math.PI, 0));
        assertEquals(LatLon.fromRadians(0, Math.PI), r1.getVertex(0));
        assertEquals(LatLon.fromRadians(0, 0), r1.getVertex(1));
        assertEquals(LatLon.fromRadians(Math.PI / 2.0, 0), r1.getVertex(2));
        assertEquals(LatLon.fromRadians(Math.PI / 2.0, Math.PI), r1.getVertex(3));

        for (int i = 0; i < 4; i++) {
            double lat = Math.PI / 4.0 * (i - 2);
            double lon = Math.PI / 2.0 * (i - 2) + 0.2;
            LatLonRect r = new LatLonRect(new LineInterval(lat, lat + Math.PI / 4.0),
                    new SphereInterval(S2Sphere.drem(lon, 2 * Math.PI),
                            S2Sphere.drem(lon + Math.PI / 2.0, 2 * Math.PI)));
            for (int k = 0; k < 4; k++) {
                assertTrue(S2Sphere.simpleCCW(r.getVertex((k - 1) & 3).toPoint(),
                        r.getVertex(k).toPoint(),
                        r.getVertex((k + 1) & 3).toPoint()));
            }
        }
    }

    @Test
    public void testContains() {
        LatLon eqM180 = LatLon.fromRadians(0, -Math.PI);
        LatLon northPole = LatLon.fromRadians(Math.PI / 2.0, 0);
        LatLonRect r1 = new LatLonRect(eqM180, northPole);

        assertTrue(r1.contains(LatLon.fromDegrees(30, -45)));
        assertTrue(r1.interiorContains(LatLon.fromDegrees(30, -45)));
        assertFalse(r1.contains(LatLon.fromDegrees(30, 45)));
        assertFalse(r1.interiorContains(LatLon.fromDegrees(30, 45)));
        assertTrue(r1.contains(eqM180));
        assertFalse(r1.interiorContains(eqM180));
        assertTrue(r1.contains(northPole));
        assertFalse(r1.interiorContains(northPole));
        assertTrue(r1.contains(new Point(0.5, -0.3, 0.1)));
        assertFalse(r1.contains(new Point(0.5, 0.2, 0.1)));
    }

    private void checkIntervalOps(LatLonRect x, LatLonRect y, String expectedRelation,
                                  LatLonRect expectedUnion, LatLonRect expectedIntersection) {
        assertEquals(x.contains(y), expectedRelation.charAt(0) == 'T');
        assertEquals(x.interiorContains(y), expectedRelation.charAt(1) == 'T');
        assertEquals(x.intersects(y), expectedRelation.charAt(2) == 'T');
        assertEquals(x.interiorIntersects(y), expectedRelation.charAt(3) == 'T');

        assertEquals(x.contains(y), x.union(y).equals(x));
        assertEquals(x.intersects(y), !x.intersection(y).isEmpty());

        assertEquals(expectedUnion, x.union(y));
        assertEquals(expectedIntersection, x.intersection(y));
    }

    @Test
    public void testIntervalOps() {
        LatLonRect r1 = rectFromDegrees(0, -180, 90, 0);

        // Test operations where one rectangle consists of a single point.
        LatLonRect r1Mid = rectFromDegrees(45, -90, 45, -90);
        checkIntervalOps(r1, r1Mid, "TTTT", r1, r1Mid);

        LatLonRect reqM180 = rectFromDegrees(0, -180, 0, -180);
        checkIntervalOps(r1, reqM180, "TFTF", r1, reqM180);

        LatLonRect rNorthPole = rectFromDegrees(90, 0, 90, 0);
        checkIntervalOps(r1, rNorthPole, "TFTF", r1, rNorthPole);

        checkIntervalOps(r1, rectFromDegrees(-10, -1, 1, 20), "FFTT",
                rectFromDegrees(-10, 180, 90, 20), rectFromDegrees(0, -1, 1, 0));

        checkIntervalOps(r1, rectFromDegrees(-10, -1, 0, 20), "FFTF",
                rectFromDegrees(-10, 180, 90, 20), rectFromDegrees(0, -1, 0, 0));

        checkIntervalOps(r1, rectFromDegrees(-10, 0, 1, 20), "FFTF",
                rectFromDegrees(-10, 180, 90, 20), rectFromDegrees(0, 0, 1, 0));

        checkIntervalOps(rectFromDegrees(-15, -160, -15, -150), rectFromDegrees(20, 145, 25, 155),
                "FFFF", rectFromDegrees(-15, 145, 25, -150), LatLonRect.empty());

        checkIntervalOps(rectFromDegrees(70, -10, 90, -140), rectFromDegrees(60, 175, 80, 5),
                "FFTT", rectFromDegrees(60, -180, 90, 180), rectFromDegrees(70, 175, 80, 5));

        checkIntervalOps(rectFromDegrees(12, 30, 60, 60), rectFromDegrees(0, 0, 30, 18), "FFFF",
                rectFromDegrees(0, 0, 60, 60), LatLonRect.empty());

        checkIntervalOps(rectFromDegrees(0, 0, 18, 42), rectFromDegrees(30, 12, 42, 60), "FFFF",
                rectFromDegrees(0, 0, 42, 60), LatLonRect.empty());
    }

    @Test
    public void testExpanded() {
        assertTrue(rectFromDegrees(70, 150, 80, 170).expanded(LatLon.fromDegrees(20, 30))
                .approxEquals(rectFromDegrees(50, 120, 90, -160)));
        assertTrue(LatLonRect.empty().expanded(LatLon.fromDegrees(20, 30)).isEmpty());
        assertTrue(LatLonRect.full().expanded(LatLon.fromDegrees(20, 30)).isFull());

        assertTrue(rectFromDegrees(-90, 170, 10, 20).expanded(LatLon.fromDegrees(30, 80))
                .approxEquals(rectFromDegrees(-90, -180, 40, 180)));
    }

    @Test
    public void testConvolveWithCap() {
        assertTrue(rectFromDegrees(0, 170, 0, -170).convolveWithCap(Angle.fromDegrees(15))
                .approxEquals(rectFromDegrees(-15, 155, 15, -155)));

        assertTrue(rectFromDegrees(60, 150, 80, 10).convolveWithCap(Angle.fromDegrees(15))
                .approxEquals(rectFromDegrees(45, -180, 90, 180)));
    }

    @Test
    public void testGetCapBound() {
        assertTrue(rectFromDegrees(-45, -45, 45, 45).getCapBound()
                .approxEquals(Cap.fromAxisHeight(new Point(1, 0, 0), 0.5)));

        assertTrue(rectFromDegrees(88, -80, 89, 80).getCapBound()
                .approxEquals(Cap.fromAxisAngle(new Point(0, 0, 1), Angle.fromDegrees(2))));

        assertTrue(rectFromDegrees(-30, -150, -10, 50).getCapBound()
                .approxEquals(Cap.fromAxisAngle(new Point(0, 0, -1), Angle.fromDegrees(80))));
    }

    private void checkCellOps(LatLonRect r, Cell cell, int level) {
        boolean vertexContained = false;
        for (int i = 0; i < 4; i++) {
            if (r.contains(cell.getVertexRaw(i)) || (!r.isEmpty() && cell.contains(r.getVertex(i).toPoint()))) {
                vertexContained = true;
            }
        }
        assertEquals(r.mayIntersect(cell), level >= 1);
        assertEquals(r.intersects(cell), level >= 2);
        assertEquals(vertexContained, level >= 3);
        assertEquals(r.contains(cell), level >= 4);
    }

    @Test
    public void testCellOps() {
        checkCellOps(LatLonRect.empty(), Cell.fromFacePosLevel(3, 0, 0), 0);
        checkCellOps(LatLonRect.full(), Cell.fromFacePosLevel(2, 0, 0), 4);
        checkCellOps(LatLonRect.full(), Cell.fromFacePosLevel(5, 0, 25), 4);

        LatLonRect r4 = rectFromDegrees(-45.1, -45.1, 0.1, 0.1);
        checkCellOps(r4, Cell.fromFacePosLevel(0, 0, 0), 3);
        checkCellOps(r4, Cell.fromFacePosLevel(0, 0, 1), 4);
        checkCellOps(r4, Cell.fromFacePosLevel(1, 0, 1), 0);

        LatLonRect r5 = rectFromDegrees(-10, -45, 10, 0);
        checkCellOps(r5, Cell.fromFacePosLevel(0, 0, 0), 3);
        checkCellOps(r5, Cell.fromFacePosLevel(0, 0, 1), 3);
        checkCellOps(r5, Cell.fromFacePosLevel(1, 0, 1), 0);

        checkCellOps(rectFromDegrees(4, 4, 4, 4), Cell.fromFacePosLevel(0, 0, 0), 3);

        checkCellOps(rectFromDegrees(41, -87, 42, -79), Cell.fromFacePosLevel(2, 0, 0), 1);
        checkCellOps(rectFromDegrees(-41, 160, -40, -160), Cell.fromFacePosLevel(5, 0, 0), 1);

        Cell cell0tr = Cell.fromPoint(new Point(1 + 1e-12, 1, 1));
        LatLonRect bound0tr = cell0tr.getRectBound();
        LatLon v0 = LatLon.fromPoint(cell0tr.getVertexRaw(0));
        checkCellOps(rectFromDegrees(v0.lat().degrees() - 1e-8, v0.lon().degrees() - 1e-8,
                        v0.lat().degrees() - 2e-10, v0.lon().degrees() + 1e-10), cell0tr, 1);

        checkCellOps(rectFromDegrees(-37, -70, -36, -20), Cell.fromFacePosLevel(5, 0, 0), 2);

        Cell cell202 = Cell.fromFacePosLevel(2, 0, 2);
        LatLonRect bound202 = cell202.getRectBound();
        checkCellOps(rectFromDegrees(bound202.latLo().degrees() + 3, bound202.lonLo().degrees() + 3,
                        bound202.latHi().degrees() - 3, bound202.lonHi().degrees() - 3), cell202, 2);
    }
}
