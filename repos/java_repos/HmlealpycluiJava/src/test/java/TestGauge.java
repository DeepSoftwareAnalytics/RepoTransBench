import org.junit.Test;
import static org.junit.Assert.assertEquals;
public class TestGauge {
    Gauge gauge = new Gauge();
    @Test
    public void testEmptyMaxValueGauge() {
        assertEquals("[]", gauge.gauge(10, 0, 100, 80));
    }
}
