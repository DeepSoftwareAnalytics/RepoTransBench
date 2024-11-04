package draw;

import utils.Utils;
import reeds_shepp.*;

import java.util.Random;

public class DrawUtils {
    private static final int SCALE = 40;

    public static int scale(int x) {
        return x * SCALE;
    }

    public static int[] scale(int[] x) {
        int[] scaled = new int[x.length];
        for (int i = 0; i < x.length; i++) {
            scaled[i] = x[i] * SCALE;
        }
        return scaled;
    }

    public static int unscale(int x) {
        return x / SCALE;
    }

    public static int[] unscale(int[] x) {
        int[] unscaled = new int[x.length];
        for (int i = 0; i < x.length; i++) {
            unscaled[i] = x[i] / SCALE;
        }
        return unscaled;
    }

    public static void setRandomPencolor() {
        Random rd = new Random();
        double r, g, b;
        do {
            r = rd.nextDouble();
            g = rd.nextDouble();
            b = rd.nextDouble();
        } while (r + g + b > 2.5);
        // Assuming there's a method to set pencolor
        setPencolor(r, g, b);
    }

    // Placeholder method for setting pen color
    private static void setPencolor(double r, double g, double b) {
        // Implement actual code to set pen color in UI library
    }
}
