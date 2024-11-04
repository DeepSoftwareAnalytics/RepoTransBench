import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestSolver {
    private static final String[] cubes = {
        "DLURRDFFUBBLDDRBRBLDLRBFRUULFBDDUFBRBBRFUDFLUDLUULFLFR",
        "GGBYOBWBBBOYRGYOYOGWROWYWGWRBRGYBGOOGBBYOYORWWRRGRWRYW",
        "BYOYYRGOWRROWGOYWGBBGOROBWGWORBBWRWYRGYBGYWOGBROYGBWYR",
        "YWYYGWWGYBBYRRBRGWOOOYWRWRBOBYROWRGOBGRWOGWBBGBGOYYGRO",
        "ROORRYOWBWWGBYGRRBYBGGGGWWOYYBRBOWBYRWOGBYORYBOWYOGRGW"
    };

    private static final String[] unsolvableCubes = {
        "ORWOWGWYWGBGRGRBOBOWYGGBRRBYBRGOWOYGRYRBBGOOBYOYRYWYWW",
        "UUUUUUUUULLLFFFRRRBBBLLLFBFRRRBFBLLLFFFRRRBBBDDDDDDDDD",
        "UUBUUUUUULLLFFFRRRUBBLLLFFFRRRBBBLLLFFFRRRBBBDDDDDDDDD",
        "UUUUUUUUULLLFFFRRRBBBLLLFFFRRRBBBLLLFFFRRBRBBDDDDDDDDD",
        "UUUUUUUUULLLFFFRRRBBBLLFLFFRRRBBBLLLFFFRRRBBBDDDDDDDDD"
    };

    @Test
    public void testCubeSolver() {
        for (String c : cubes) {
            checkCanSolveCube(c);
        }
    }

    private void checkCanSolveCube(String orig) {
        Cube c = new Cube(orig);
        Solver solver = new Solver(c);
        try {
            solver.solve();
            assertTrue(c.isSolved(), "Failed to solve cube: " + orig);
        } catch (Exception e) {
            fail(e.getMessage() + " original cube: " + orig);
        }
    }

    @Test
    public void testUnsolvableCube() {
        for (String c : unsolvableCubes) {
            checkCubeFailsToSolve(c);
        }
    }

    private void checkCubeFailsToSolve(String orig) {
        Cube c = new Cube(orig);
        Solver solver = new Solver(c);
        assertThrows(Exception.class, solver::solve, "Stuck in loop - unsolvable cube");
    }
}
