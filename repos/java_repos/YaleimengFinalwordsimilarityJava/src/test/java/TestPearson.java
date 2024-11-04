import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import Pearson.Pearson;

public class TestPearson {

    @Test
    public void testCalPearson() {
        double[] x = {1, 2, 3, 4, 5};
        double[] y = {2, 4, 6, 8, 10};
        double expectedPearson = 1.0; // Perfect positive correlation
        double result = Pearson.calPearson(x, y);
        assertEquals(expectedPearson, result, 1e-5, "Pearson correlation should be close to 1.0");
    }

    @Test
    public void testCalPearsonNoCorrelation() {
        double[] x = {1, 2, 3, 4, 5};
        double[] y = {5, 4, 3, 2, 1};
        double expectedPearson = -1.0; // Perfect negative correlation
        double result = Pearson.calPearson(x, y);
        assertEquals(expectedPearson, result, 1e-5, "Pearson correlation should be close to -1.0");
    }

    @Test
    public void testCalPearsonPartialCorrelation() {
        double[] x = {1, 2, 3, 4, 5};
        double[] y = {1, 2, 3, 4, 6};
        double expectedPearson = 0.986; // Manually calculated correlation
        double result = Pearson.calPearson(x, y);
        assertEquals(expectedPearson, Math.round(result * 1000.0) / 1000.0, 1e-5, "Pearson correlation should be close to 0.986");
    }
}
