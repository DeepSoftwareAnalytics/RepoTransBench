package utils;

public class Utils {

    public static double M(double theta) {
        theta = theta % (2 * Math.PI);
        if (theta < -Math.PI) return theta + 2 * Math.PI;
        if (theta >= Math.PI) return theta - 2 * Math.PI;
        return theta;
    }

    public static double[] R(double x, double y) {
        double r = Math.sqrt(x * x + y * y);
        double theta = Math.atan2(y, x);
        return new double[]{r, theta};
    }

    public static double[] changeOfBasis(double[] p1, double[] p2) {
        double theta1 = deg2rad(p1[2]);
        double dx = p2[0] - p1[0];
        double dy = p2[1] - p1[1];
        double new_x = dx * Math.cos(theta1) + dy * Math.sin(theta1);
        double new_y = -dx * Math.sin(theta1) + dy * Math.cos(theta1);
        double new_theta = p2[2] - p1[2];
        return new double[]{new_x, new_y, new_theta};
    }

    public static double rad2deg(double rad) {
        return 180 * rad / Math.PI;
    }

    public static double deg2rad(double deg) {
        return Math.PI * deg / 180;
    }

    public static int sign(double x) {
        return x >= 0 ? 1 : -1;
    }
}
