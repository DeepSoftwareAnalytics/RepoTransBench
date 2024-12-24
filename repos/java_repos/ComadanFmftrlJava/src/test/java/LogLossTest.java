import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

public class LogLossTest {

    @Test
    public void testLogLoss1() {
        assertEquals(-Math.log(0.9), LogLoss.logLoss(0.9, 1), 1e-6);
    }

    @Test
    public void testLogLoss0() {
        assertEquals(-Math.log(0.9), LogLoss.logLoss(0.1, 0), 1e-6);
    }

    @Test
    public void testLogLossEdge1() {
        assertEquals(-Math.log(1.0 - 1e-15), LogLoss.logLoss(1.0, 1), 1e-6);
    }

    @Test
    public void testLogLossEdge0() {
        assertEquals(-Math.log(1.0 - 1e-15), LogLoss.logLoss(0.0, 0), 1e-6);
    }

    @Test
    public void testLogLossMid() {
        assertEquals(-Math.log(0.5), LogLoss.logLoss(0.5, 1), 1e-6);
    }

    @Test
    public void testLogLossMidNegative() {
        assertEquals(-Math.log(0.5), LogLoss.logLoss(0.5, 0), 1e-6);
    }
}
