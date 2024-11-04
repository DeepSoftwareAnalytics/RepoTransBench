import java.util.Random;

public class Latency {
    private int latency;

    public Latency() {
        Random random = new Random(Settings.seed);
        this.latency = random.nextInt(901) + 100;  // Random value between 100 and 1000 ms
    }

    public String action() {
        return "netem delay " + latency + "ms";
    }

    public String desc() {
        return "delay of " + latency + "ms";
    }

    public int getLatency() {
        return latency;
    }
}
