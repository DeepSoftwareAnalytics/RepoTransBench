import org.junit.Test;
import static org.junit.Assert.*;

public class Glicko2Test {

    private static class Almost {
        private final Rating val;
        private final int precision;

        public Almost(Rating val, int precision) {
            this.val = val;
            this.precision = precision;
        }

        public Almost(Rating val) {
            this(val, 3);
        }

        private boolean almostEquals(double val1, double val2) {
            if (Math.round(val1 * Math.pow(10, precision)) == Math.round(val2 * Math.pow(10, precision))) {
                return true;
            }
            String format = "%." + precision + "f";
            int mantissa1 = Integer.parseInt(String.format(format, val1).replace(".", ""));
            int mantissa2 = Integer.parseInt(String.format(format, val2).replace(".", ""));
            return Math.abs(mantissa1 - mantissa2) <= 1;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Almost almost = (Almost) obj;
            Rating other = almost.val;
            try {
                if (!almostEquals(val.getVolatility(), other.getVolatility())) {
                    return false;
                }
            } catch (Exception e) {
                // Handle exception if needed
            }
            return almostEquals(val.getMu(), other.getMu()) && almostEquals(val.getSigma(), other.getSigma());
        }

        @Override
        public String toString() {
            return val.toString();
        }
    }

    @Test
    public void testGlickmanExample() {
        Glicko2 env = new Glicko2(0.5);
        Rating r1 = env.createRating(1500, 200, 0.06);
        Rating r2 = env.createRating(1400, 30);
        Rating r3 = env.createRating(1550, 100);
        Rating r4 = env.createRating(1700, 300);
        Rating rated = env.rate(r1, new Object[]{Result.WIN, r2, Result.LOSS, r3, Result.LOSS, r4});
        assertEquals(new Almost(rated), new Almost(env.createRating(1464.051, 151.515, 0.05999)));
    }
}
