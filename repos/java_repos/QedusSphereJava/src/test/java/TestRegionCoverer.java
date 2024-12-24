import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class TestRegionCoverer {

    private Random random;

    @Before
    public void setUp() {
        random = new Random(20);
    }

    @Test
    public void testRandomCells() {
        for (int i = 0; i < 10; i++) {  // 使用10次迭代作为示例
            RegionCoverer coverer = new RegionCoverer();
            coverer.setMaxCells(1);
            CellId cellId = TestCellId.getRandomCellId();

            List<CellId> covering = coverer.getCovering(new Cell(cellId));
            assertEquals(1, covering.size());
            assertEquals(cellId, covering.get(0));
        }
    }

    private int skewed(int maxLong) {
        int base = random.nextInt(maxLong + 1);
        return random.nextInt((1 << base) - 1);
    }

    private Point randomPoint() {
        double x = 2 * random.nextDouble() - 1;
        double y = 2 * random.nextDouble() - 1;
        double z = 2 * random.nextDouble() - 1;
        return new Point(x, y, z).normalize();
    }

    private Cap getRandomCap(double minArea, double maxArea) {
        double capArea = maxArea * Math.pow(minArea / maxArea, random.nextDouble());
        assert capArea >= minArea && capArea <= maxArea;
        return Cap.fromAxisArea(randomPoint(), capArea);
    }

    private void checkCellUnionCovering(Region region, CellUnion covering, boolean checkTight, CellId cellId) {
        if (!cellId.isValid()) {
            for (int face = 0; face < 6; face++) {
                checkCellUnionCovering(region, covering, checkTight, CellId.fromFacePosLevel(face, 0, 0));
            }
            return;
        }
        if (!region.mayIntersect(new Cell(cellId))) {
            if (checkTight) {
                assertFalse(covering.intersects(cellId));
            }
        } else if (!covering.contains(cellId)) {
            assertFalse(region.contains(new Cell(cellId)));
            assertFalse(cellId.isLeaf());
            for (CellId child : cellId.children()) {
                checkCellUnionCovering(region, covering, checkTight, child);
            }
        }
    }

    private void checkCovering(RegionCoverer coverer, Region region, List<CellId> covering, boolean interior) {
        Map<CellId, Integer> minLevelCells = new HashMap<>();
        for (CellId cell : covering) {
            int level = cell.level();
            assertTrue(level >= coverer.getMinLevel());
            assertTrue(level <= coverer.getMaxLevel());
            assertEquals((level - coverer.getMinLevel()) % coverer.getLevelMod(), 0);
            minLevelCells.merge(cell.parent(coverer.getMinLevel()), 1, Integer::sum);
        }
        if (covering.size() > coverer.getMaxCells()) {
            for (int count : minLevelCells.values()) {
                assertEquals(1, count);
            }
        }

        if (interior) {
            for (CellId cellId : covering) {
                assertTrue(region.contains(new Cell(cellId)));
            }
        } else {
            CellUnion cellUnion = new CellUnion(covering);
            checkCellUnionCovering(region, cellUnion, true, CellId.none());
        }
    }

    @Test
    public void testRandomCaps() {
        for (int i = 0; i < 10; i++) {  // 使用10次迭代作为示例
            RegionCoverer coverer = new RegionCoverer();

            coverer.setMinLevel(random.nextInt(CellId.MAX_LEVEL + 1));
            coverer.setMaxLevel(random.nextInt(CellId.MAX_LEVEL + 1));

            while (coverer.getMinLevel() > coverer.getMaxLevel()) {
                coverer.setMinLevel(random.nextInt(CellId.MAX_LEVEL + 1));
                coverer.setMaxLevel(random.nextInt(CellId.MAX_LEVEL + 1));
            }

            coverer.setMaxCells(skewed(10));
            coverer.setLevelMod(1 + random.nextInt(3));

            double maxArea = Math.min(4 * Math.PI, (3 * coverer.getMaxCells() + 1) * CellId.avgArea().getValue(coverer.getMinLevel()));

            Cap cap = getRandomCap(0.1 * CellId.avgArea().getValue(CellId.MAX_LEVEL), maxArea);
            List<CellId> covering = coverer.getCovering(cap);
            checkCovering(coverer, cap, covering, false);

            List<CellId> interior = coverer.getInteriorCovering(cap);
            checkCovering(coverer, cap, interior, true);

            // Check deterministic.
            List<CellId> covering2 = coverer.getCovering(cap);
            covering.sort(null);
            covering2.sort(null);
            assertEquals(covering, covering2);

            CellUnion cells = new CellUnion(covering);
            List<CellId> denormalized = cells.denormalize(coverer.getMinLevel(), coverer.getLevelMod());
            checkCovering(coverer, cap, denormalized, false);
        }
    }

    @Test
    public void testSimpleCoverings() {
        for (int i = 0; i < 10; i++) {  // 使用10次迭代作为示例
            RegionCoverer coverer = new RegionCoverer();
            coverer.setMaxCells(Integer.MAX_VALUE);
            int level = random.nextInt(CellId.MAX_LEVEL + 1);
            coverer.setMinLevel(level);
            coverer.setMaxLevel(level);

            double maxArea = Math.min(4 * Math.PI, 1000 * CellId.avgArea().getValue(level));
            Cap cap = getRandomCap(0.1 * CellId.avgArea().getValue(CellId.MAX_LEVEL), maxArea);

            List<CellId> covering = RegionCoverer.getSimpleCovering(cap, cap.axis(), level);
            checkCovering(coverer, cap, covering, false);
        }
    }
}
