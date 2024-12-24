import java.util.Random;

public class Reorder {
    private int delay;
    private int reorder;
    private int correlation;

    public Reorder() {
        Random random = new Random(Settings.seed);
        this.delay = 10; // Constant delay
        this.reorder = random.nextInt(66) + 10; // Random value between 10 and 75
        this.correlation = 50; // Constant correlation
    }

    public String action() {
        return "netem delay " + delay + "ms reorder " + (100 - reorder) + "% " + correlation + "%";
    }

    public String desc() {
        return "reorder after delay of " + delay + "ms with probability " + (100 - reorder) + " and correlation " + correlation;
    }

    public int getDelay() {
        return delay;
    }

    public int getReorder() {
        return reorder;
    }

    public int getCorrelation() {
        return correlation;
    }
}
