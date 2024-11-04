import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import java.util.*;
import s2sphere.*;

public class TestCell {

    private LevelStats[] levelStats;

    @Before
    public void setUp() {
        Random random = new Random(20);
        levelStats = new LevelStats[CellId.MAX_LEVEL + 1];
        for (int i = 0; i <= CellId.MAX_LEVEL; i++) {
            levelStats[i] = new LevelStats();
        }
        // 如果需要性能分析器，在Java中可能需要其他工具（如JMH）
    }

    @After
    public void tearDown() {
        levelStats = null; // 清理资源
    }

    @Test
    public void testFaces() {
        Map<Point, Integer> edgeCounts = new HashMap<>();
        Map<Point, Integer> vertexCounts = new HashMap<>();

        for (int face = 0; face < 6; face++) {
            CellId cellId = CellId.fromFacePosLevel(face, 0, 0);
            Cell cell = new Cell(cellId);

            assertEquals(cellId, cell.id());
            assertEquals(face, cell.face());
            assertEquals(0, cell.level());

            // 检查面朝向
            assertEquals(face & s2sphere.SWAP_MASK, cell.orientation());
            assertFalse(cell.isLeaf());

            for (int k = 0; k < 4; k++) {
                Point edge = cell.getEdgeRaw(k);
                edgeCounts.put(edge, edgeCounts.getOrDefault(edge, 0) + 1);
                
                Point vertex = cell.getVertexRaw(k);
                vertexCounts.put(vertex, vertexCounts.getOrDefault(vertex, 0) + 1);

                assertEquals(0.0, vertex.dotProd(edge), 0.000001);
                assertEquals(0.0, cell.getVertexRaw((k + 1) & 3).dotProd(edge), 0.000001);
                assertEquals(1.0, cell.getVertexRaw(k)
                        .crossProd(cell.getVertexRaw((k + 1) & 3))
                        .normalize().dotProd(cell.getEdge(k)), 0.000001);
            }
        }

        // 检查边的计数是否为2，顶点计数是否为3
        for (int count : edgeCounts.values()) {
            assertEquals(2, count);
        }

        for (int count : vertexCounts.values()) {
            assertEquals(3, count);
        }
    }

    private void gatherStats(Cell cell) {
        LevelStats s = levelStats[cell.level()];
        double exactArea = cell.exactArea();
        double approxArea = cell.approxArea();
        double minEdge = 100, maxEdge = 0, avgEdge = 0;
        double minDiag = 100, maxDiag = 0;
        double minWidth = 100, maxWidth = 0;
        double minAngleSpan = 100, maxAngleSpan = 0;

        for (int i = 0; i < 4; i++) {
            double edge = cell.getVertexRaw(i).angle(cell.getVertexRaw((i + 1) & 3));
            minEdge = Math.min(edge, minEdge);
            maxEdge = Math.max(edge, maxEdge);
            avgEdge += 0.25 * edge;

            Point mid = cell.getVertexRaw(i).add(cell.getVertexRaw((i + 1) & 3));
            double width = Math.PI / 2.0 - mid.angle(cell.getEdgeRaw(i ^ 2));
            minWidth = Math.min(width, minWidth);
            maxWidth = Math.max(width, maxWidth);

            if (i < 2) {
                double diag = cell.getVertexRaw(i).angle(cell.getVertexRaw(i ^ 2));
                minDiag = Math.min(diag, minDiag);
                maxDiag = Math.max(diag, maxDiag);
                double angleSpan = cell.getEdgeRaw(i).angle(cell.getEdgeRaw(i ^ 2).negate());
                minAngleSpan = Math.min(angleSpan, minAngleSpan);
                maxAngleSpan = Math.max(angleSpan, maxAngleSpan);
            }
        }

        s.setCount(s.getCount() + 1);
        s.setMinArea(Math.min(exactArea, s.getMinArea()));
        s.setMaxArea(Math.max(exactArea, s.getMaxArea()));
        s.setAvgArea(s.getAvgArea() + exactArea);
        s.setMinWidth(Math.min(minWidth, s.getMinWidth()));
        s.setMaxWidth(Math.max(maxWidth, s.getMaxWidth()));
        s.setAvgWidth(s.getAvgWidth() + 0.5 * (minWidth + maxWidth));
        s.setMinEdge(Math.min(minEdge, s.getMinEdge()));
        s.setMaxEdge(Math.max(maxEdge, s.getMaxEdge()));
        s.setAvgEdge(s.getAvgEdge() + avgEdge);
        s.setMaxEdgeAspect(Math.max(maxEdge / minEdge, s.getMaxEdgeAspect()));
        s.setMinDiag(Math.min(minDiag, s.getMinDiag()));
        s.setMaxDiag(Math.max(maxDiag, s.getMaxDiag()));
        s.setAvgDiag(s.getAvgDiag() + 0.5 * (minDiag + maxDiag));
        s.setMaxDiagAspect(Math.max(maxDiag / minDiag, s.getMaxDiagAspect()));
        s.setMinAngleSpan(Math.min(minAngleSpan, s.getMinAngleSpan()));
        s.setMaxAngleSpan(Math.max(maxAngleSpan, s.getMaxAngleSpan()));
        s.setAvgAngleSpan(s.getAvgAngleSpan() + 0.5 * (minAngleSpan + maxAngleSpan));

        double approxRatio = approxArea / exactArea;
        s.setMinApproxRatio(Math.min(approxRatio, s.getMinApproxRatio()));
        s.setMaxApproxRatio(Math.max(approxRatio, s.getMaxApproxRatio()));
    }

    private void checkSubdivide(Cell cell) {
        gatherStats(cell);
        if (cell.isLeaf()) return;

        Cell[] children = cell.subdivide();

        double exactArea = 0;
        double approxArea = 0;
        double averageArea = 0;

        for (int i = 0; i < children.length; i++) {
            Cell child = children[i];
            exactArea += child.exactArea();
            approxArea += child.approxArea();
            averageArea += child.averageArea();

            assertEquals(child.id(), cell.id().children()[i]);
            assertTrue(child.getCenter().angle(child.id().toPoint()) < 1e-15);

            Cell direct = new Cell(child.id());
            assertEquals(direct.face(), child.face());
            assertEquals(direct.level(), child.level());
            assertEquals(direct.orientation(), child.orientation());
            assertEquals(direct.getCenterRaw(), child.getCenterRaw());

            for (int k = 0; k < 4; k++) {
                assertEquals(direct.getVertexRaw(k), child.getVertexRaw(k));
                assertEquals(direct.getEdgeRaw(k), child.getEdgeRaw(k));
            }

            // 检查contains()和may_intersect()方法
            assertTrue(cell.contains(child));
            assertTrue(cell.mayIntersect(child));
            assertFalse(child.contains(cell));
            assertTrue(cell.contains(child.getCenterRaw()));

            for (int j = 0; j < 4; j++) {
                assertTrue(cell.contains(child.getVertexRaw(j)));
                if (i != j) {
                    assertFalse(child.contains(children[j].getCenterRaw()));
                    assertFalse(child.mayIntersect(children[j]));
                }
            }

            // 检查边界
            Cap parentCap = cell.getCapBound();
            Rect parentRect = cell.getRectBound();
            if (cell.contains(new Point(0, 0, 1)) || cell.contains(new Point(0, 0, -1))) {
                assertTrue(parentRect.lon().isFull());
            }

            Cap childCap = children[i].getCapBound();
            Rect childRect = children[i].getRectBound();

            assertTrue(childCap.contains(children[i].getCenter()));
            assertTrue(childRect.contains(children[i].getCenterRaw()));
            assertTrue(parentCap.contains(children[i].getCenter()));
            assertTrue(parentRect.contains(children[i].getCenterRaw()));

            for (int j = 0; j < 4; j++) {
                assertTrue(childCap.contains(children[i].getVertex(j)));
                assertTrue(childRect.contains(children[i].getVertexRaw(j)));
                assertTrue(parentCap.contains(children[i].getVertex(j)));
                assertTrue(parentRect.contains(children[i].getVertexRaw(j)));

                if (j != i) {
                    int capCount = 0;
                    int rectCount = 0;
                    for (int k = 0; k < 4; k++) {
                        if (childCap.contains(children[j].getVertex(k))) {
                            capCount++;
                        }
                        if (childRect.contains(children[j].getVertexRaw(k))) {
                            rectCount++;
                        }
                    }
                    assertTrue(capCount <= 2);
                    if (childRect.latLo().radians() > -Math.PI / 2.0 &&
                        childRect.latHi().radians() < Math.PI / 2.0) {
                        assertTrue(rectCount <= 2);
                    }
                }
            }

            boolean forceSubdivide = false;
            Point center = s2sphere.getNorm(children[i].face());
            Point edge = center.add(s2sphere.getUAxis(children[i].face()));
            Point corner = edge.add(s2sphere.getVAxis(children[i].face()));

            for (int j = 0; j < 4; j++) {
                Point p = children[i].getVertexRaw(j);
                if (p.equals(center) || p.equals(edge) || p.equals(corner)) {
                    forceSubdivide = true;
                }
            }

            if (forceSubdivide || cell.level() < 5 || new Random().nextInt(50) == 0) {
                checkSubdivide(children[i]);
            }
        }

        // 检查面积
        assertTrue(Math.abs(Math.log(exactArea / cell.exactArea())) <= Math.abs(Math.log(1 + 1e-6)));
        assertTrue(Math.abs(Math.log(approxArea / cell.approxArea())) <= Math.abs(Math.log(1.03)));
        assertTrue(Math.abs(Math.log(averageArea / cell.averageArea())) <= Math.abs(Math.log(1 + 1e-15)));
    }

    @Test
    public void testSubdivide() {
        for (int face = 0; face < 6; face++) {
            checkSubdivide(new Cell(CellId.fromFacePosLevel(face, 0, 0)));
        }
    }
}
