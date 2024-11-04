import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class TestOptimize {
    private static final String[][] moves = {
        {"R", "Ri"}, {"L", "Li"}, {"U", "Ui"}, {"D", "Di"}, {"F", "Fi"}, {"B", "Bi"},
        {"M", "Mi"}, {"E", "Ei"}, {"S", "Si"}, {"X", "Xi"}, {"Y", "Yi"}, {"Z", "Zi"}
    };

    private static final String solvedCubeStr = 
        "    UUU\n" +
        "    UUU\n" +
        "    UUU\n" +
        "LLL FFF RRR BBB\n" +
        "LLL FFF RRR BBB\n" +
        "LLL FFF RRR BBB\n" +
        "    DDD\n" +
        "    DDD\n" +
        "    DDD";

    @Test
    public void testOptimizeRepeatThree() {
        for (String[] move : moves) {
            String cw = move[0];
            String cc = move[1];
            assertEquals(Collections.singletonList(cc), Optimize.optimizeMoves(Arrays.asList(cw, cw, cw)));
            assertEquals(Collections.singletonList(cw), Optimize.optimizeMoves(Arrays.asList(cc, cc, cc)));
            assertEquals(Arrays.asList("_", cw), Optimize.optimizeMoves(Arrays.asList("_", cc, cc, cc)));
            assertEquals(Arrays.asList("_", cc), Optimize.optimizeMoves(Arrays.asList("_", cw, cw, cw)));
            assertEquals(Arrays.asList("_", cw, "_"), Optimize.optimizeMoves(Arrays.asList("_", cc, cc, cc, "_")));
            assertEquals(Arrays.asList("_", cc, "_"), Optimize.optimizeMoves(Arrays.asList("_", cw, cw, cw, "_")));

            assertEquals(Arrays.asList(cw, cw), Optimize.optimizeMoves(Arrays.asList(cc, cc, cc, cc, cc, cc)));
            assertEquals(Arrays.asList(cw, cw, "_"), Optimize.optimizeMoves(Arrays.asList(cc, cc, cc, cc, cc, cc, "_")));
            assertEquals(Arrays.asList(cw, cw, "_", "_"), Optimize.optimizeMoves(Arrays.asList(cc, cc, cc, cc, cc, cc, "_", "_")));
            assertEquals(Collections.singletonList(cc), Optimize.optimizeMoves(Arrays.asList(cc, cc, cc, cc, cc, cc, cc, cc, cc)));
        }
    }

    @Test
    public void testOptimizeDoUndo() {
        for (String[] move : moves) {
            String cw = move[0];
            String cc = move[1];
            assertEquals(Collections.emptyList(), Optimize.optimizeMoves(Arrays.asList(cc, cw)));
            assertEquals(Collections.emptyList(), Optimize.optimizeMoves(Arrays.asList(cw, cc)));

            assertEquals(Collections.emptyList(), Optimize.optimizeMoves(Arrays.asList(cw, cw, cc, cc)));
            assertEquals(Collections.emptyList(), Optimize.optimizeMoves(Arrays.asList(cw, cw, cw, cc, cc, cc)));
            assertEquals(Collections.emptyList(), Optimize.optimizeMoves(Arrays.asList(cw, cw, cw, cw, cc, cc, cc, cc)));

            assertEquals(Arrays.asList("1", "2"), Optimize.optimizeMoves(Arrays.asList("1", cw, cw, cc, cc, "2")));
            assertEquals(Arrays.asList("1", "2", "3", "4"), Optimize.optimizeMoves(Arrays.asList("1", "2", cw, cw, cc, cc, "3", "4")));
        }
    }

    @Test
    public void testFullCubeRotationOptimization() {
        for (String[] move : new String[][]{{"X", "Xi"}, {"Y", "Yi"}, {"Z", "Zi"}}) {
            String cw = move[0];
            String cc = move[1];
            for (List<String> moves : Arrays.asList(Arrays.asList(cc, cw), Arrays.asList(cw, cc))) {
                Optimize.applyNoFullCubeRotationOptimization(moves);
                assertEquals(Collections.emptyList(), moves);
            }
        }

        for (String[] move : new String[][]{{"Z", "Zi"}}) {
            String cw = move[0];
            String cc = move[1];
            List<String> moves = Arrays.asList(cw, "U", "L", "D", "R", "E", "M", cc);
            List<String> expected = Arrays.asList("L", "D", "R", "U", "Mi", "E");
            List<String> actual = new ArrayList<>(moves);
            Optimize.applyNoFullCubeRotationOptimization(actual);
            assertEquals(expected, actual);

            Cube c = new Cube(solvedCubeStr);
            Cube d = new Cube(solvedCubeStr);
            c.sequence(String.join(" ", moves));
            d.sequence(String.join(" ", actual));
            assertEquals(c, d);

            moves = Arrays.asList(cw, cw, "U", "L", "D", "R", "E", "M", cc, cc);
            expected = Arrays.asList("D", "R", "U", "L", "Ei", "Mi");
            actual = new ArrayList<>(moves);
            Optimize.applyNoFullCubeRotationOptimization(actual);
            assertEquals(expected, actual);

            c = new Cube(solvedCubeStr);
            d = new Cube(solvedCubeStr);
            c.sequence(String.join(" ", moves));
            d.sequence(String.join(" ", actual));
            assertEquals(c, d);
        }
    }
}
