import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class TestSphereInterval {

    private SphereInterval empty, full, zero, pi2, pi, mipi, mipi2, quad1, quad2, quad3, quad4;
    private SphereInterval quad12, quad23, quad34, quad41, quad123, quad234, quad341, quad412;
    private SphereInterval mid12, mid23, mid34, mid41;

    @Before
    public void setUp() {
        empty = SphereInterval.empty();
        full = SphereInterval.full();

        zero = new SphereInterval(0, 0);
        pi2 = new SphereInterval(Math.PI / 2.0, Math.PI / 2.0);
        pi = new SphereInterval(Math.PI, Math.PI);
        mipi = new SphereInterval(-Math.PI, -Math.PI);
        mipi2 = new SphereInterval(-Math.PI / 2.0, -Math.PI / 2.0);
        // 单个象限
        quad1 = new SphereInterval(0, Math.PI / 2.0);
        quad2 = new SphereInterval(Math.PI / 2.0, -Math.PI);
        quad3 = new SphereInterval(Math.PI, -Math.PI / 2.0);
        quad4 = new SphereInterval(-Math.PI / 2.0, 0);
        // 象限对
        quad12 = new SphereInterval(0, -Math.PI);
        quad23 = new SphereInterval(Math.PI / 2.0, -Math.PI / 2.0);
        quad34 = new SphereInterval(-Math.PI, 0);
        quad41 = new SphereInterval(-Math.PI / 2.0, Math.PI / 2.0);
        // 象限三角
        quad123 = new SphereInterval(0, -Math.PI / 2.0);
        quad234 = new SphereInterval(Math.PI / 2.0, 0);
        quad341 = new SphereInterval(Math.PI, Math.PI / 2.0);
        quad412 = new SphereInterval(-Math.PI / 2.0, -Math.PI);
        // 中点偏移
        mid12 = new SphereInterval(Math.PI / 2 - 0.01, Math.PI / 2 + 0.02);
        mid23 = new SphereInterval(Math.PI - 0.01, -Math.PI + 0.02);
        mid34 = new SphereInterval(-Math.PI / 2.0 - 0.01, -Math.PI / 2.0 + 0.02);
        mid41 = new SphereInterval(-0.01, 0.02);
    }

    @Test
    public void testConstructorsAndAccessors() {
        assertEquals(quad12.lo(), 0, 0.000001);
        assertEquals(quad12.hi(), Math.PI, 0.000001);
        assertEquals(quad34.bound(0), Math.PI, 0.000001);
        assertEquals(quad34.bound(1), 0, 0.000001);
        assertEquals(pi.lo(), Math.PI, 0.000001);
        assertEquals(pi.hi(), Math.PI, 0.000001);

        // 检查 [-Pi, -Pi] 是否被归一化为 [Pi, Pi].
        assertEquals(mipi.lo(), Math.PI, 0.000001);
        assertEquals(mipi.hi(), Math.PI, 0.000001);
        assertEquals(quad23.lo(), Math.PI / 2.0, 0.000001);
        assertEquals(quad23.hi(), -Math.PI / 2.0, 0.000001);

        SphereInterval defaultEmpty = new SphereInterval();
        assertTrue(defaultEmpty.isValid());
        assertTrue(defaultEmpty.isEmpty());
        assertEquals(empty.lo(), defaultEmpty.lo(), 0.000001);
        assertEquals(empty.hi(), defaultEmpty.hi(), 0.000001);
    }

    @Test
    public void testSimplePredicates() {
        // is_valid(), is_empty(), is_full(), is_inverted()
        assertTrue(zero.isValid() && !zero.isEmpty() && !zero.isFull());
        assertTrue(empty.isValid() && empty.isEmpty() && !empty.isFull());
        assertTrue(empty.isInverted());
        assertTrue(full.isValid() && !full.isEmpty() && full.isFull());
        assertTrue(!quad12.isEmpty() && !quad12.isFull() && !quad12.isInverted());
        assertTrue(!quad23.isEmpty() && !quad23.isFull() && quad23.isInverted());
        assertTrue(pi.isValid() && !pi.isEmpty() && !pi.isInverted());
        assertTrue(mipi.isValid() && !mipi.isEmpty() && !mipi.isInverted());
    }

    @Test
    public void testGetCenter() {
        assertEquals(quad12.getCenter(), Math.PI / 2.0, 0.000001);
        assertEquals(new SphereInterval(3.1, 2.9).getCenter(), 3.0 - Math.PI, 0.000001);
        assertEquals(new SphereInterval(-2.9, -3.1).getCenter(), Math.PI - 3.0, 0.000001);
        assertEquals(new SphereInterval(2.1, -2.1).getCenter(), Math.PI, 0.000001);
        assertEquals(pi.getCenter(), Math.PI, 0.000001);
        assertEquals(mipi.getCenter(), Math.PI, 0.000001);
        assertEquals(quad123.getCenter(), 0.75 * Math.PI, 0.000001);
    }

    @Test
    public void testGetLength() {
        assertEquals(quad12.getLength(), Math.PI, 0.000001);
        assertEquals(pi.getLength(), 0, 0.000001);
        assertEquals(mipi.getLength(), 0, 0.000001);
        assertEquals(quad123.getLength(), 1.5 * Math.PI, 0.000001);
        assertEquals(Math.abs(quad23.getLength()), Math.PI, 0.000001);
        assertEquals(full.getLength(), 2 * Math.PI, 0.000001);
        assertTrue(empty.getLength() < 0);
    }

    @Test
    public void testComplement() {
        assertTrue(empty.complement().isFull());
        assertTrue(full.complement().isEmpty());
        assertTrue(pi.complement().isFull());
        assertTrue(mipi.complement().isFull());
        assertTrue(zero.complement().isFull());
        assertTrue(quad12.complement().approxEquals(quad34));
        assertTrue(quad34.complement().approxEquals(quad12));
        assertTrue(quad123.complement().approxEquals(quad4));
    }

    @Test
    public void testContains() {
        assertTrue(!empty.contains(0) && !empty.contains(Math.PI) && !empty.contains(-Math.PI));
        assertTrue(!empty.interiorContains(Math.PI) && !empty.interiorContains(-Math.PI));
        assertTrue(full.contains(0) && full.contains(Math.PI) && full.contains(-Math.PI));
        assertTrue(full.interiorContains(Math.PI) && full.interiorContains(-Math.PI));
        assertTrue(quad12.contains(0) && quad12.contains(Math.PI) && quad12.contains(-Math.PI));
        assertTrue(quad12.interiorContains(Math.PI / 2.0) && !quad12.interiorContains(0));
        assertTrue(!quad12.interiorContains(Math.PI) && !quad12.interiorContains(-Math.PI));
        assertTrue(quad23.contains(Math.PI / 2.0) && quad23.contains(-Math.PI / 2.0));
        assertTrue(quad23.contains(Math.PI) && quad23.contains(-Math.PI));
        assertTrue(!quad23.contains(0));
        assertTrue(!quad23.interiorContains(Math.PI / 2.0) && !quad23.interiorContains(-Math.PI / 2.0));
        assertTrue(quad23.interiorContains(Math.PI) && quad23.interiorContains(-Math.PI));
        assertTrue(!quad23.interiorContains(0));
        assertTrue(pi.contains(Math.PI) && pi.contains(-Math.PI) && !pi.contains(0));
        assertTrue(!pi.interiorContains(Math.PI) && !pi.interiorContains(-Math.PI));
        assertTrue(mipi.contains(Math.PI) && mipi.contains(-Math.PI) && !mipi.contains(0));
        assertTrue(!mipi.interiorContains(Math.PI) && !mipi.interiorContains(-Math.PI));
        assertTrue(zero.contains(0) && !zero.interiorContains(0));
    }

    public void checkIntervalOps(SphereInterval x, SphereInterval y, 
                                  String expectedRelation, 
                                  SphereInterval expectedUnion, 
                                  SphereInterval expectedIntersection) {
        assertEquals(x.contains(y), expectedRelation.charAt(0) == 'T');
        assertEquals(x.interiorContains(y), expectedRelation.charAt(1) == 'T');
        assertEquals(x.intersects(y), expectedRelation.charAt(2) == 'T');
        assertEquals(x.interiorIntersects(y), expectedRelation.charAt(3) == 'T');

        assertEquals(x.union(y).bounds(), expectedUnion.bounds());
        assertEquals(x.intersection(y).bounds(), expectedIntersection.bounds());

        assertEquals(x.contains(y), x.union(y).equals(x));
        assertEquals(x.intersects(y), !x.intersection(y).isEmpty());
    }

    @Test
    public void testIntervalOps() {
        checkIntervalOps(empty, empty, "TTFF", empty, empty);
        checkIntervalOps(empty, full, "FFFF", full, empty);
        checkIntervalOps(empty, zero, "FFFF", zero, empty);
        checkIntervalOps(empty, pi, "FFFF", pi, empty);
        checkIntervalOps(empty, mipi, "FFFF", mipi, empty);
        checkIntervalOps(full, empty, "TTFF", full, empty);
        checkIntervalOps(full, full, "TTTT", full, full);
        checkIntervalOps(full, zero, "TTTT", full, zero);
        checkIntervalOps(full, pi, "TTTT", full, pi);
        checkIntervalOps(full, mipi, "TTTT", full, mipi);
        checkIntervalOps(full, quad12, "TTTT", full, quad12);
        checkIntervalOps(full, quad23, "TTTT", full, quad23);

        checkIntervalOps(zero, empty, "TTFF", zero, empty);
        checkIntervalOps(zero, full, "FFTF", full, zero);
        checkIntervalOps(zero, zero, "TFTF", zero, zero);
        checkIntervalOps(zero, pi, "FFFF", new SphereInterval(0, Math.PI), empty);
        checkIntervalOps(zero, pi2, "FFFF", quad1, empty);
        checkIntervalOps(zero, mipi, "FFFF", quad12, empty);
        checkIntervalOps(zero, mipi2, "FFFF", quad4, empty);
        checkIntervalOps(zero, quad12, "FFTF", quad12, zero);
        checkIntervalOps(zero, quad23, "FFFF", quad123, empty);

        checkIntervalOps(pi2, empty, "TTFF", pi2, empty);
        checkIntervalOps(pi2, full, "FFTF", full, pi2);
        checkIntervalOps(pi2, zero, "FFFF", quad1, empty);
        checkIntervalOps(pi2, pi, "FFFF", new SphereInterval(Math.PI / 2.0, Math.PI), empty);
        checkIntervalOps(pi2, pi2, "TFTF", pi2, pi2);
        checkIntervalOps(pi2, mipi, "FFFF", quad2, empty);
        checkIntervalOps(pi2, mipi2, "FFFF", quad23, empty);
        checkIntervalOps(pi2, quad12, "FFTF", quad12, pi2);
        checkIntervalOps(pi2, quad23, "FFTF", quad23, pi2);

        checkIntervalOps(pi, empty, "TTFF", pi, empty);
        checkIntervalOps(pi, full, "FFTF", full, pi);
        checkIntervalOps(pi, zero, "FFFF", new SphereInterval(Math.PI, 0), empty);
        checkIntervalOps(pi, pi, "TFTF", pi, pi);
        checkIntervalOps(pi, pi2, "FFFF", new SphereInterval(Math.PI / 2.0, Math.PI), empty);
        checkIntervalOps(pi, mipi, "TFTF", pi, pi);
        checkIntervalOps(pi, mipi2, "FFFF", quad3, empty);
        checkIntervalOps(pi, quad12, "FFTF", new SphereInterval(0, Math.PI), pi);
        checkIntervalOps(pi, quad23, "FFTF", quad23, pi);

        checkIntervalOps(mipi, empty, "TTFF", mipi, empty);
        checkIntervalOps(mipi, full, "FFTF", full, mipi);
        checkIntervalOps(mipi, zero, "FFFF", quad34, empty);
        checkIntervalOps(mipi, pi, "TFTF", mipi, mipi);
        checkIntervalOps(mipi, pi2, "FFFF", quad2, empty);
        checkIntervalOps(mipi, mipi, "TFTF", mipi, mipi);
        checkIntervalOps(mipi, mipi2, "FFFF", new SphereInterval(-Math.PI, -Math.PI / 2.0), empty);
        checkIntervalOps(mipi, quad12, "FFTF", quad12, mipi);
        checkIntervalOps(mipi, quad23, "FFTF", quad23, mipi);

        checkIntervalOps(quad12, empty, "TTFF", quad12, empty);
        checkIntervalOps(quad12, full, "FFTT", full, quad12);
        checkIntervalOps(quad12, zero, "TFTF", quad12, zero);
        checkIntervalOps(quad12, pi, "TFTF", quad12, pi);
        checkIntervalOps(quad12, mipi, "TFTF", quad12, mipi);
        checkIntervalOps(quad12, quad12, "TFTT", quad12, quad12);
        checkIntervalOps(quad12, quad23, "FFTT", quad123, quad2);
        checkIntervalOps(quad12, quad34, "FFTF", full, quad12);

        checkIntervalOps(quad23, empty, "TTFF", quad23, empty);
        checkIntervalOps(quad23, full, "FFTT", full, quad23);
        checkIntervalOps(quad23, zero, "FFFF", quad234, empty);
        checkIntervalOps(quad23, pi, "TTTT", quad23, pi);
        checkIntervalOps(quad23, mipi, "TTTT", quad23, mipi);
        checkIntervalOps(quad23, quad12, "FFTT", quad123, quad2);
        checkIntervalOps(quad23, quad23, "TFTT", quad23, quad23);
        checkIntervalOps(quad23, quad34, "FFTT", quad234, new SphereInterval(-Math.PI, -Math.PI / 2.0));

        checkIntervalOps(quad1, quad23, "FFTF", quad123, new SphereInterval(Math.PI / 2.0, Math.PI / 2.0));
        checkIntervalOps(quad2, quad3, "FFTF", quad23, mipi);
        checkIntervalOps(quad3, quad2, "FFTF", quad23, pi);
        checkIntervalOps(quad2, pi, "TFTF", quad2, pi);
        checkIntervalOps(quad2, mipi, "TFTF", quad2, mipi);
        checkIntervalOps(quad3, pi, "TFTF", quad3, pi);
        checkIntervalOps(quad3, mipi, "TFTF", quad3, mipi);

        checkIntervalOps(quad12, mid12, "TTTT", quad12, mid12);
        checkIntervalOps(mid12, quad12, "FFTT", quad12, mid12);

        SphereInterval quad12eps = new SphereInterval(quad12.lo(), mid23.hi());
        SphereInterval quad2hi = new SphereInterval(mid23.lo(), quad12.hi());
        checkIntervalOps(quad12, mid23, "FFTT", quad12eps, quad2hi);
        checkIntervalOps(mid23, quad12, "FFTT", quad12eps, quad2hi);

        SphereInterval quad412eps = new SphereInterval(mid34.lo(), quad12.hi());
        checkIntervalOps(quad12, mid34, "FFFF", quad412eps, empty);
        checkIntervalOps(mid34, quad12, "FFFF", quad412eps, empty);

        SphereInterval quadeps12 = new SphereInterval(mid41.lo(), quad12.hi());
        SphereInterval quad1lo = new SphereInterval(quad12.lo(), mid41.hi());
        checkIntervalOps(quad12, mid41, "FFTT", quadeps12, quad1lo);
        checkIntervalOps(mid41, quad12, "FFTT", quadeps12, quad1lo);

        SphereInterval quad2lo = new SphereInterval(quad23.lo(), mid12.hi());
        SphereInterval quad3hi = new SphereInterval(mid34.lo(), quad23.hi());
        SphereInterval quadeps23 = new SphereInterval(mid12.lo(), quad23.hi());
        SphereInterval quad23eps = new SphereInterval(quad23.lo(), mid34.hi());
        SphereInterval quadeps123 = new SphereInterval(mid41.lo(), quad23.hi());
        checkIntervalOps(quad23, mid12, "FFTT", quadeps23, quad2lo);
        checkIntervalOps(mid12, quad23, "FFTT", quadeps23, quad2lo);
        checkIntervalOps(quad23, mid23, "TTTT", quad23, mid23);
        checkIntervalOps(mid23, quad23, "FFTT", quad23, mid23);
        checkIntervalOps(quad23, mid34, "FFTT", quad23eps, quad3hi);
        checkIntervalOps(mid34, quad23, "FFTT", quad23eps, quad3hi);
        checkIntervalOps(quad23, mid41, "FFFF", quadeps123, empty);
        checkIntervalOps(mid41, quad23, "FFFF", quadeps123, empty);
    }

    @Test
    public void testFromPointPair() {
        assertEquals(SphereInterval.fromPointPair(-Math.PI, Math.PI), pi);
        assertEquals(SphereInterval.fromPointPair(Math.PI, -Math.PI), pi);
        assertEquals(SphereInterval.fromPointPair(mid34.hi(), mid34.lo()), mid34);
        assertEquals(SphereInterval.fromPointPair(mid23.lo(), mid23.hi()), mid23);
    }

    @Test
    public void testExpanded() {
        assertEquals(empty.expanded(1), empty);
        assertEquals(full.expanded(1), full);
        assertEquals(zero.expanded(1), new SphereInterval(-1, 1));
        assertEquals(mipi.expanded(0.01), new SphereInterval(Math.PI - 0.01, -Math.PI + 0.01));
        assertEquals(pi.expanded(27), full);
        assertEquals(pi.expanded(Math.PI / 2.0), quad23);
        assertEquals(pi2.expanded(Math.PI / 2.0), quad12);
        assertEquals(mipi2.expanded(Math.PI / 2.0), quad34);
    }

    @Test
    public void testApproxEquals() {
        assertTrue(empty.approxEquals(empty));
        assertTrue(zero.approxEquals(empty) && empty.approxEquals(zero));
        assertTrue(pi.approxEquals(empty) && empty.approxEquals(pi));
        assertTrue(mipi.approxEquals(empty) && empty.approxEquals(mipi));
        assertTrue(pi.approxEquals(mipi) && mipi.approxEquals(pi));
        assertTrue(pi.union(mipi).approxEquals(pi));
        assertTrue(mipi.union(pi).approxEquals(pi));
        assertTrue(pi.union(mid12).union(zero).approxEquals(quad12));
        assertTrue(quad2.intersection(quad3).approxEquals(pi));
        assertTrue(quad3.intersection(quad2).approxEquals(pi));
    }

    @Test
    public void testGetDirectedHausdorffDistance() {
        assertEquals(0.0, empty.getDirectedHausdorffDistance(empty), 0.000001);
        assertEquals(0.0, empty.getDirectedHausdorffDistance(mid12), 0.000001);
        assertEquals(Math.PI, mid12.getDirectedHausdorffDistance(empty), 0.000001);

        assertEquals(0.0, quad12.getDirectedHausdorffDistance(quad123), 0.000001);

        SphereInterval interval = new SphereInterval(3.0, -3.0);
        assertEquals(3.0, new SphereInterval(-0.1, 0.2).getDirectedHausdorffDistance(interval), 0.000001);
        assertEquals(3.0 - 0.1, new SphereInterval(0.1, 0.2).getDirectedHausdorffDistance(interval), 0.000001);
        assertEquals(3.0 - 0.1, new SphereInterval(-0.2, -0.1).getDirectedHausdorffDistance(interval), 0.000001);
    }
}
