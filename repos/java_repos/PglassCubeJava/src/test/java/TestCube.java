import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestCube {
    @Test
    public void testCubeIsSolved() {
        String solvedCubeStr = "    UUU\n"
                             + "    UUU\n"
                             + "    UUU\n"
                             + "LLL FFF RRR BBB\n"
                             + "LLL FFF RRR BBB\n"
                             + "LLL FFF RRR BBB\n"
                             + "    DDD\n"
                             + "    DDD\n"
                             + "    DDD";
        Cube solvedCube = new Cube(solvedCubeStr);
        assertTrue(solvedCube.isSolved());
    }
}
