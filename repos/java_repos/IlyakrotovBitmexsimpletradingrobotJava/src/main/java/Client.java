public class Client {

    public interface Order {
        void Order_new(String symbol, String side, int orderQty) throws Exception;
    }

    private final Order Order;

    public Client(Order Order) {
        this.Order = Order;
    }

    public Order getOrder() {
        return Order;
    }
}
