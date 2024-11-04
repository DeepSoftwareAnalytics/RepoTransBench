import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import s2sphere.*;

public class TestCellId {
    public static CellId getRandomCellId(int... args) {
        Random random = new Random();
        int level;
        if (args.length == 0) {
            level = random.nextInt(CellId.MAX_LEVEL + 1);
        } else {
            level = args[0];
        }
        int face = random.nextInt(CellId.NUM_FACES);
        long pos = random.nextLong() & ((1L << (2 * CellId.MAX_LEVEL)) - 1);
        return CellId.fromFacePosLevel(face, pos, level);
    }

    public Point getRandomPoint() {
        Random random = new Random();
        double x = 2 * random.nextDouble() - 1;
        double y = 2 * random.nextDouble() - 1;
        double z = 2 * random.nextDouble() - 1;
        return new Point(x, y, z).normalize();
    }

    public static CellId getCellId(double lat, double lon) {
        return CellId.fromLatLon(LatLon.fromDegrees(lat, lon));
    }

    @Test
    public void testDefaultConstructor() {
        CellId cellId = new CellId();
        assertEquals(0, cellId.id());
        assertFalse(cellId.isValid());
    }

    @Test
    public void testFaceDefinitions() {
        assertEquals(0, getCellId(0, 0).face());
        assertEquals(1, getCellId(0, 90).face());
        assertEquals(2, getCellId(90, 0).face());
        assertEquals(3, getCellId(0, 180).face());
        assertEquals(4, getCellId(0, -90).face());
        assertEquals(5, getCellId(-90, 0).face());
    }

    @Test
    public void testParentChildRelationships() {
        CellId cellId = CellId.fromFacePosLevel(3, 0x12345678, CellId.MAX_LEVEL - 4);

        assertTrue(cellId.isValid());
        assertEquals(3, cellId.face());
        assertEquals(0x12345700, cellId.pos());
        assertEquals(CellId.MAX_LEVEL - 4, cellId.level());
        assertFalse(cellId.isLeaf());

        assertEquals(0x12345610, cellId.childBegin(cellId.level() + 2).pos());
        assertEquals(0x12345640, cellId.childBegin().pos());
        assertEquals(0x12345400, cellId.parent().pos());
        assertEquals(0x12345000, cellId.parent(cellId.level() - 2).pos());

        // 检查子节点与父节点的顺序
        assertTrue(cellId.childBegin().compareTo(cellId) < 0);
        assertTrue(cellId.childEnd().compareTo(cellId) > 0);
        assertEquals(cellId.childBegin().next().next().next().next(), cellId.childEnd());
        assertEquals(cellId.childBegin(CellId.MAX_LEVEL), cellId.rangeMin());
        assertEquals(cellId.childEnd(CellId.MAX_LEVEL), cellId.rangeMax().next());

        // 检查Hilbert曲线的中心位置
        assertEquals(cellId.rangeMin().id() + cellId.rangeMax().id(), 2 * cellId.id());
    }

    @Test
    public void testWrapping() {
        assertEquals(CellId.begin(0).prevWrap(), CellId.end(0).prev());
        assertEquals(
            CellId.begin(CellId.MAX_LEVEL).prevWrap(),
            CellId.fromFacePosLevel(5, 0xffffffffffffffffL >> CellId.FACE_BITS, CellId.MAX_LEVEL)
        );

        assertEquals(
            CellId.begin(CellId.MAX_LEVEL).advanceWrap(-1),
            CellId.fromFacePosLevel(5, 0xffffffffffffffffL >> CellId.FACE_BITS, CellId.MAX_LEVEL)
        );

        assertEquals(CellId.end(4).advance(-1).advanceWrap(1), CellId.begin(4));
        assertEquals(CellId.end(CellId.MAX_LEVEL).advance(-1).advanceWrap(1),
                     CellId.fromFacePosLevel(0, 0, CellId.MAX_LEVEL));
        assertEquals(CellId.end(4).prev().nextWrap(), CellId.begin(4));
        assertEquals(CellId.end(CellId.MAX_LEVEL).prev().nextWrap(),
                     CellId.fromFacePosLevel(0, 0, CellId.MAX_LEVEL));
    }

    @Test
    public void testAdvance() {
        CellId cellId = CellId.fromFacePosLevel(3, 0x12345678, CellId.MAX_LEVEL - 4);

        assertEquals(CellId.begin(0).advance(7), CellId.end(0));
        assertEquals(CellId.begin(0).advance(12), CellId.end(0));
        assertEquals(CellId.end(0).advance(-7), CellId.begin(0));
        assertEquals(CellId.end(0).advance(-12000000), CellId.begin(0));

        int numLevel5Cells = 6 << (2 * 5);
        assertEquals(CellId.begin(5).advance(500),
                     CellId.end(5).advance(500 - numLevel5Cells));
        assertEquals(cellId.childBegin(CellId.MAX_LEVEL).advance(256),
                     cellId.next().childBegin(CellId.MAX_LEVEL));
        assertEquals(CellId.fromFacePosLevel(1, 0, CellId.MAX_LEVEL)
                     .advance(4 << (2 * CellId.MAX_LEVEL)),
                     CellId.fromFacePosLevel(5, 0, CellId.MAX_LEVEL));

        // 检查advanceWrap()的基本属性
        assertEquals(CellId.begin(0).advanceWrap(7),
                     CellId.fromFacePosLevel(1, 0, 0));
        assertEquals(CellId.begin(0).advanceWrap(12), CellId.begin(0));

        assertEquals(CellId.fromFacePosLevel(5, 0, 0).advanceWrap(-7),
                     CellId.fromFacePosLevel(4, 0, 0));
        assertEquals(CellId.begin(0).advanceWrap(-12000000), CellId.begin(0));
        assertEquals(CellId.begin(5).advanceWrap(6644),
                     CellId.begin(5).advanceWrap(-11788));
        assertEquals(
            cellId.childBegin(CellId.MAX_LEVEL).advanceWrap(256),
            cellId.next().childBegin(CellId.MAX_LEVEL));
        assertEquals(
            CellId.fromFacePosLevel(5, 0, CellId.MAX_LEVEL)
            .advanceWrap(2 << (2 * CellId.MAX_LEVEL)),
            CellId.fromFacePosLevel(1, 0, CellId.MAX_LEVEL));
    }

    @Test
    public void testInverse() {
        for (int i = 0; i < INVERSE_ITERATIONS; i++) {
            CellId cellId = getRandomCellId(CellId.MAX_LEVEL);
            assertTrue(cellId.isLeaf());
            assertEquals(CellId.MAX_LEVEL, cellId.level());
            LatLon center = cellId.toLatLon();
            assertEquals(CellId.fromLatLon(center).id(), cellId.id());
        }
    }

    @Test
    public void testTokens() {
        for (int i = 0; i < TOKEN_ITERATIONS; i++) {
            CellId cellId = getRandomCellId();
            String token = cellId.toToken();
            assertTrue(token.length() <= 16);
            assertEquals(cellId, CellId.fromToken(token));
        }
    }

    @Test
    public void testNeighbors() {
        // 检查面1的边邻居
        int[] outFaces = {5, 3, 2, 0};
        CellId[] faceNbrs = CellId.fromFacePosLevel(1, 0, 0).getEdgeNeighbors();
        for (int i = 0; i < faceNbrs.length; i++) {
            assertTrue(faceNbrs[i].isFace());
            assertEquals(outFaces[i], faceNbrs[i].face());
        }

        // 检查面2的中心的顶点邻居（5级）
        CellId[] neighbors = CellId.fromPoint(new Point(0, 0, 1)).getVertexNeighbors(5);
        Arrays.sort(neighbors);
        for (int i = 0; i < neighbors.length; i++) {
            assertEquals(
                neighbors[i],
                CellId.fromFaceIj(2, (1 << 29) - (i < 2 ? 1 : 0), (1 << 29) - (i == 0 || i == 3 ? 1 : 0)).parent(5)
            );
        }

        // 检查面0, 4, 5的交界处的顶点邻居
        CellId cellId = CellId.fromFacePosLevel(0, 0, CellId.MAX_LEVEL);
        neighbors = cellId.getVertexNeighbors(0);
        Arrays.sort(neighbors);
        assertEquals(3, neighbors.length);
        assertEquals(neighbors[0], CellId.fromFacePosLevel(0, 0, 0));
        assertEquals(neighbors[1], CellId.fromFacePosLevel(4, 0, 0));
        assertEquals(neighbors[2], CellId.fromFacePosLevel(5, 0, 0));

        for (int i = 0; i < NEIGHBORS_ITERATIONS; i++) {
            cellId = getRandomCellId();
            if (cellId.isLeaf()) {
                cellId = cellId.parent();
            }
            int maxDiff = Math.min(6, CellId.MAX_LEVEL - cellId.level() - 1);
            int level = maxDiff == 0 ? cellId.level() : cellId.level() + new Random().nextInt(maxDiff);
            checkAllNeighbors(cellId, level);
        }
    }

    public void checkAllNeighbors(CellId cellId, int level) {
        assertTrue(level >= cellId.level());
        assertTrue(level < CellId.MAX_LEVEL);

        Set<CellId> all = new HashSet<>();
        Set<CellId> expected = new HashSet<>();

        Collections.addAll(all, cellId.getAllNeighbors(level));
        for (CellId child : cellId.children(level + 1)) {
            all.add(child.parent());
            expected.addAll(Arrays.asList(child.getVertexNeighbors(level)));
        }

        assertEquals(expected, all);
    }
}
