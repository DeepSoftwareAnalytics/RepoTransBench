import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;

public class Settings {
    public static final int seed = 1;
    public static final int delay = 1;
    public static final double p_fault = 0.5;
    public static final boolean debug = false;
    public static final String[] interfaces = {"eth0"};
    public static final int[] ports = {2001};
    public static final Level log_level = Level.INFO;

    public static final Map<Class<?>, Double> faults = new HashMap<Class<?>, Double>() {
        {
            put(Partition.class, 0.2);
            put(PacketLoss.class, 0.2);
            put(Latency.class, 0.3);
            put(Reorder.class, 0.3);
        }
    };
}
