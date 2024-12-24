import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestTrader {

    private Client client;
    private Strategy strategy;
    private Trader trader;
    private Client.Order order;

    @Before
    public void setUp() {
        client = mock(Client.class);
        strategy = mock(Strategy.class);
        order = mock(Client.Order.class);
        trader = new Trader(client, strategy, 100, 5);
    }

    @Test
    public void testInitialization() {
        assertNotNull(trader);
    }

    @Test
    public void testInitialMoneyToTrade() {
        assertEquals(100, trader.getMoneyToTrade());
    }

    @Test
    public void testInitialLeverage() {
        assertEquals(5, trader.getLeverage());
    }

    @Test
    public void testExecuteTradeBuy() throws Exception {
        when(strategy.predict()).thenReturn(1);
        trader.executeTrade();
        verify(order).Order_new("XBTUSD", "Buy", 500);
    }

    @Test
    public void testExecuteTradeSell() throws Exception {
        when(strategy.predict()).thenReturn(-1);
        trader.executeTrade();
        verify(order).Order_new("XBTUSD", "Sell", 500);
    }

    @Test
    public void testExecuteTradeDoNothing() throws Exception {
        when(strategy.predict()).thenReturn(0);
        trader.executeTrade();
        verify(order, never()).Order_new(anyString(), anyString(), anyInt());
    }

    @Test(expected = Exception.class)
    public void testExecuteTradeExceptionHandling() throws Exception {
        when(strategy.predict()).thenReturn(1);
        doThrow(new Exception("API Error")).when(order).Order_new(anyString(), anyString(), anyInt());
        trader.executeTrade();
    }

    @Test
    public void testExecuteTradeLeverage() throws Exception {
        trader = new Trader(client, strategy, 200, 10);
        when(strategy.predict()).thenReturn(1);
        trader.executeTrade();
        verify(order).Order_new("XBTUSD", "Buy", 2000);
    }

    @Test
    public void testUpdateMoneyToTrade() {
        trader.setMoneyToTrade(200);
        assertEquals(200, trader.getMoneyToTrade());
    }

    @Test
    public void testUpdateLeverage() {
        trader.setLeverage(10);
        assertEquals(10, trader.getLeverage());
    }
}
