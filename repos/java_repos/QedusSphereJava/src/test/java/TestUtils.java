import static org.junit.Assert.*;
import org.junit.Test;

public class TestUtils {

    @Test
    public void testDrem() {
        // Assuming there is a S2Sphere class that contains the drem method
        assertEquals(-0.4, S2Sphere.drem(6.5, 2.3), 1e-9);
        assertEquals(1.0, S2Sphere.drem(1.0, 2.0), 1e-9);
        assertEquals(1.0, S2Sphere.drem(1.0, 3.0), 1e-9);
    }
}
