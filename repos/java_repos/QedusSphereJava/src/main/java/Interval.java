package com.example;

public class Interval {
    private final double lo;
    private final double hi;

    public Interval(double lo, double hi) {
        this.lo = lo;
        this.hi = hi;
    }

    @Override
    public String toString() {
        return String.format("%s: (%f, %f)", this.getClass().getSimpleName(), lo, hi);
    }

    public double getLo() {
        return lo;
    }

    public double getHi() {
        return hi;
    }

    public double getBound(int i) {
        if (i == 0) {
            return lo;
        } else if (i == 1) {
            return hi;
        } else {
            throw new IndexOutOfBoundsException("Invalid index: " + i);
        }
    }

    public double[] getBounds() {
        return new double[]{lo, hi};
    }

    public static Interval empty() {
        return new Interval(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }
}
