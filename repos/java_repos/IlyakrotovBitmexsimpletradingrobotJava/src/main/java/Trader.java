public class Trader {
    private final Client client;
    private final Strategy strategy;
    private int moneyToTrade;
    private int leverage;

    public Trader(Client client, Strategy strategy, int moneyToTrade, int leverage) {
        this.client = client;
        this.strategy = strategy;
        this.moneyToTrade = moneyToTrade;
        this.leverage = leverage;
    }

    public void executeTrade() {
        int prediction = strategy.predict();

        System.out.println("Last prediction: " + prediction);

        try {
            if (prediction == -1) {
                client.getOrder().Order_new("XBTUSD", "Sell", moneyToTrade * leverage);
            } else if (prediction == 1) {
                client.getOrder().Order_new("XBTUSD", "Buy", moneyToTrade * leverage);
            }
        } catch (Exception e) {
            System.out.println("Something goes wrong!");
        }
    }

    public int getMoneyToTrade() {
        return moneyToTrade;
    }

    public void setMoneyToTrade(int moneyToTrade) {
        this.moneyToTrade = moneyToTrade;
    }

    public int getLeverage() {
        return leverage;
    }

    public void setLeverage(int leverage) {
        this.leverage = leverage;
    }
}
