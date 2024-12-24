package com.example;

public class Metric {
    private final double deriv;
    private final int dim;

    public Metric(double deriv, int dim) {
        this.deriv = deriv;
        this.dim = dim;
    }

    public double getDeriv() {
        return deriv;
    }

    public double getValue(int level) {
        return Math.scalb(deriv, -dim * level);
    }

    public int getClosestLevel(double value) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public int getMinLevel(double value) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public int getMaxLevel(double value) {
        if (value <= 0) {
            return CellId.MAX_LEVEL;
        }

        double[] frexpResult = frexp(deriv / value);
        double m = frexpResult[0];
        int x = (int) frexpResult[1];
        int level = Math.max(0, Math.min(CellId.MAX_LEVEL, (x - 1) >> (dim - 1)));
        assert level == 0 || getValue(level) >= value;
        assert level == CellId.MAX_LEVEL || getValue(level + 1) < value;
        return level;
    }

    private double[] frexp(double value) {
        long bits = Double.doubleToLongBits(value);
        int exponent = (int) ((bits >> 52) & 0x7FF);
        if (exponent == 0) {
            // Subnormal number
            value *= Double.longBitsToDouble(0x4350000000000000L); // 2^54
            bits = Double.doubleToLongBits(value);
            exponent = (int) ((bits >> 52) & 0x7FF) - 54;
        }
        exponent -= 1022;
        double mantissa = Double.longBitsToDouble((bits & 0x800FFFFFFFFFFFFFL) | 0x3FE0000000000000L);
        return new double[]{mantissa, exponent};
    }
}
