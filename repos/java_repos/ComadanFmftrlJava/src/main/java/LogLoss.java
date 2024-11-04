import static java.lang.Math.log;

public class LogLoss {
    public static double logLoss(double p, double y) {
        p = Math.max(Math.min(p, 1.0 - 1e-15), 1e-15);
        return y == 1.0 ? -log(p) : -log(1.0 - p);
    }
}
