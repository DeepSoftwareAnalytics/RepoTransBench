import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

public class TestConfiguration {

    @Test
    public void testTimeToWaitNewTrade1m() {
        assertEquals(60, TimeToWaitNewTrade.getTimeToWait("1m"));
    }

    @Test
    public void testTimeToWaitNewTrade5m() {
        assertEquals(300, TimeToWaitNewTrade.getTimeToWait("5m"));
    }

    @Test
    public void testTimeToWaitNewTrade1h() {
        assertEquals(3600, TimeToWaitNewTrade.getTimeToWait("1h"));
    }

    @Test
    public void testTimeToWaitNewTrade1d() {
        assertEquals(86400, TimeToWaitNewTrade.getTimeToWait("1d"));
    }

    @Test
    public void testTimeToWaitNewTradeKeys() {
        Map<String, Integer> expectedKeys = new HashMap<>();
        expectedKeys.put("1m", 60);
        expectedKeys.put("5m", 300);
        expectedKeys.put("1h", 3600);
        expectedKeys.put("1d", 86400);
        assertEquals(expectedKeys.keySet(), TimeToWaitNewTrade.getTimeToWaitMap().keySet());
    }
}
