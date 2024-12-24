import static org.junit.Assert.*;
import org.junit.Test;

public class TestLineInterval {

    private void checkIntervalOps(LineInterval x, LineInterval y, String expected) {
        assertEquals(expected.charAt(0) == 'T', x.contains(y));
        assertEquals(expected.charAt(1) == 'T', x.interiorContains(y));
        assertEquals(expected.charAt(2) == 'T', x.intersects(y));
        assertEquals(expected.charAt(3) == 'T', x.interiorIntersects(y));
    }

    @Test
    public void testBasic() {
        LineInterval unit = new LineInterval(0, 1);
        LineInterval negUnit = new LineInterval(-1, 0);
        assertEquals(0, unit.getLo(), 0.000001);
        assertEquals(1, unit.getHi(), 0.000001);
        assertEquals(-1, negUnit.bound(0), 0.000001);
        assertEquals(0, negUnit.bound(1), 0.000001);

        // 创建一个从0.5到0.5的线段，长度为0
        LineInterval half = new LineInterval(0.5, 0.5);
        assertFalse(unit.isEmpty());
        assertFalse(half.isEmpty());

        // 空区间
        LineInterval empty = LineInterval.empty();
        assertTrue(empty.isEmpty());

        // 默认空区间
        LineInterval defaultEmpty = new LineInterval();
        assertTrue(defaultEmpty.isEmpty());
        assertEquals(empty.getLo(), defaultEmpty.getLo(), 0.000001);
        assertEquals(empty.getHi(), defaultEmpty.getHi(), 0.000001);

        // 测试getCenter() 和 getLength()
        assertEquals(0.5, unit.getCenter(), 0.000001);
        assertEquals(0.5, half.getCenter(), 0.000001);
        assertEquals(1.0, negUnit.getLength(), 0.000001);
        assertTrue(empty.getLength() < 0);

        // Contains 和 InteriorContains 方法的测试
        assertTrue(unit.contains(0.5));
        assertTrue(unit.interiorContains(0.5));
        assertTrue(unit.contains(0));
        assertFalse(unit.interiorContains(0));
        assertTrue(unit.contains(1));
        assertFalse(unit.interiorContains(1));

        // 测试区间操作
        checkIntervalOps(empty, empty, "TTFF");
        checkIntervalOps(empty, unit, "FFFF");
        checkIntervalOps(unit, half, "TTTT");
        checkIntervalOps(unit, unit, "TFTT");
        checkIntervalOps(unit, empty, "TTFF");
        checkIntervalOps(unit, negUnit, "FFTF");
        checkIntervalOps(unit, new LineInterval(0, 0.5), "TFTT");
        checkIntervalOps(half, new LineInterval(0, 0.5), "FFTF");

        // 测试 fromPointPair 静态方法
        assertEquals(new LineInterval(4, 4), LineInterval.fromPointPair(4, 4));
        assertEquals(new LineInterval(-2, -1), LineInterval.fromPointPair(-1, -2));
        assertEquals(new LineInterval(-5, 3), LineInterval.fromPointPair(-5, 3));

        // 测试 expanded() 方法
        assertEquals(empty, empty.expanded(0.45));
        assertEquals(new LineInterval(-0.5, 1.5), unit.expanded(0.5));

        // 测试 union 和 intersection 方法
        assertEquals(new LineInterval(99, 100), new LineInterval(99, 100).union(empty));
        assertEquals(new LineInterval(99, 100), empty.union(new LineInterval(99, 100)));
        assertTrue(new LineInterval(5, 3).union(new LineInterval(0, -2)).isEmpty());
        assertTrue(new LineInterval(0, -2).union(new LineInterval(5, 3)).isEmpty());
        assertEquals(unit, unit.union(unit));
        assertEquals(new LineInterval(-1, 1), unit.union(negUnit));
        assertEquals(new LineInterval(-1, 1), negUnit.union(unit));
        assertEquals(unit, half.union(unit));
        assertEquals(half, unit.intersection(half));
        assertTrue(negUnit.intersection(half).isEmpty());
        assertTrue(unit.intersection(empty).isEmpty());
        assertTrue(empty.intersection(unit).isEmpty());
    }
}
