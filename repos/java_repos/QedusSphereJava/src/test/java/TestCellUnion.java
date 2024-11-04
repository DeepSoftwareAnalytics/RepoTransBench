import static org.junit.Assert.*;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestCellUnion {

    private Random random = new Random();

    @Test
    public void testBasic() {
        CellUnion empty = new CellUnion(new ArrayList<>());
        assertEquals(0, empty.numCells());

        CellId face1Id = CellId.fromFacePosLevel(1, 0, 0);
        List<CellId> face1List = new ArrayList<>();
        face1List.add(face1Id);
        CellUnion face1Union = new CellUnion(face1List);
        assertEquals(1, face1Union.numCells());
        assertEquals(face1Id, face1Union.cellId(0));

        CellId face2Id = CellId.fromFacePosLevel(2, 0, 0);
        List<Long> face2List = new ArrayList<>();
        face2List.add(face2Id.id());
        CellUnion face2Union = new CellUnion(face2List);
        assertEquals(1, face2Union.numCells());
        assertEquals(face2Id, face2Union.cellId(0));

        Cell face1Cell = new Cell(face1Id);
        Cell face2Cell = new Cell(face2Id);
        assertTrue(face1Union.contains(face1Cell));
        assertFalse(face1Union.contains(face2Cell));
    }

    private void addCells(CellId cellId, boolean selected, List<CellId> input, List<CellId> expected) {
        if (cellId == CellId.none()) {
            for (int face = 0; face < 6; face++) {
                addCells(CellId.fromFacePosLevel(face, 0, 0), false, input, expected);
            }
            return;
        }

        if (cellId.isLeaf()) {
            assertTrue(selected);
            input.add(cellId);
            return;
        }

        if (!selected && random.nextInt(CellId.MAX_LEVEL - cellId.level()) == 0) {
            expected.add(cellId);
            selected = true;
        }

        boolean added = false;
        if (selected && random.nextInt(6) != 0) {
            input.add(cellId);
            added = true;
        }

        int numChildren = 0;
        for (CellId child : cellId.children()) {
            int crange = selected ? 12 : 4;
            if (random.nextInt(crange) == 0 && numChildren < 3) {
                addCells(child, selected, input, expected);
                numChildren++;
            }
        }

        if (selected && !added) {
            addCells(cellId, selected, input, expected);
        }
    }

    @Test
    public void testNormalize() {
        for (int i = 0; i < 20; i++) { // Example iteration count (20 for testing)
            List<CellId> input = new ArrayList<>();
            List<CellId> expected = new ArrayList<>();
            addCells(CellId.none(), false, input, expected);

            CellUnion cellUnion = new CellUnion(input);
            assertEquals(expected.size(), cellUnion.numCells());

            for (int j = 0; j < expected.size(); j++) {
                assertEquals(expected.get(j), cellUnion.cellId(j));
            }

            // Additional checks for containment and intersection with cells
            for (CellId inputId : input) {
                assertTrue(cellUnion.contains(inputId));
                assertTrue(cellUnion.contains(inputId.toPoint()));
                assertTrue(cellUnion.intersects(inputId));

                if (!inputId.isFace()) {
                    assertTrue(cellUnion.intersects(inputId.parent()));
                    if (inputId.level() > 1) {
                        assertTrue(cellUnion.intersects(inputId.parent().parent()));
                        assertTrue(cellUnion.intersects(inputId.parent(0)));
                    }
                }

                if (!inputId.isLeaf()) {
                    assertTrue(cellUnion.contains(inputId.childBegin()));
                    assertTrue(cellUnion.intersects(inputId.childBegin()));
                    assertTrue(cellUnion.contains(inputId.childEnd().prev()));
                    assertTrue(cellUnion.intersects(inputId.childEnd().prev()));
                    assertTrue(cellUnion.contains(inputId.childBegin(CellId.MAX_LEVEL)));
                    assertTrue(cellUnion.intersects(inputId.childBegin(CellId.MAX_LEVEL)));
                }
            }
        }
    }

    @Test
    public void testEmpty() {
        CellUnion emptyCellUnion = new CellUnion();
        CellId face1Id = CellId.fromFacePosLevel(1, 0, 0);

        // Normalize
        emptyCellUnion.normalize();
        assertEquals(0, emptyCellUnion.numCells());

        // Denormalize
        assertEquals(0, emptyCellUnion.numCells());

        // Contains
        assertFalse(emptyCellUnion.contains(face1Id));
        assertTrue(emptyCellUnion.contains(emptyCellUnion));

        // Intersects
        assertFalse(emptyCellUnion.intersects(face1Id));
        assertFalse(emptyCellUnion.intersects(emptyCellUnion));

        // GetUnion
        CellUnion union = CellUnion.getUnion(emptyCellUnion, emptyCellUnion);
        assertEquals(0, union.numCells());

        // GetIntersection
        CellUnion intersection = CellUnion.getIntersection(emptyCellUnion, face1Id);
        assertEquals(0, intersection.numCells());
        intersection = CellUnion.getIntersection(emptyCellUnion, emptyCellUnion);
        assertEquals(0, intersection.numCells());

        // GetDifference
        CellUnion difference = CellUnion.getDifference(emptyCellUnion, emptyCellUnion);
        assertEquals(0, difference.numCells());

        // Expand
        emptyCellUnion.expand(Angle.fromRadians(1), 20);
        assertEquals(0, emptyCellUnion.numCells());
        emptyCellUnion.expand(10);
        assertEquals(0, emptyCellUnion.numCells());
    }
}
