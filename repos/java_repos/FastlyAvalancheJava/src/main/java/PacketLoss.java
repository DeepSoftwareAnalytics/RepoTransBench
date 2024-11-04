import java.util.Random;

public class PacketLoss {
    private int loss;

    public PacketLoss() {
        Random random = new Random(Settings.seed);
        this.loss = random.nextInt(6) + 5;  // Random value between 5 and 10
    }

    public String action() {
        return "netem loss " + loss + "%";
    }

    public String desc() {
        return "drop packets with probability " + loss + "%";
    }

    public int getLoss() {
        return loss;
    }
}
