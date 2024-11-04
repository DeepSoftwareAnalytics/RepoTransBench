package com.example;

import java.util.Objects;

public class SphereInterval extends Interval {

    public SphereInterval() {
        super(Math.PI, -Math.PI);
    }

    public SphereInterval(double lo, double hi) {
        this(lo, hi, false);
    }

    public SphereInterval(double lo, double hi, boolean argsChecked) {
        super(argsChecked ? lo : clamp(lo), argsChecked ? hi : clamp(hi));
        assert isValid();
    }

    private static double clamp(double value) {
        if (value == -Math.PI) {
            return Math.PI;
        }
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SphereInterval that = (SphereInterval) obj;
        return Double.compare(that.getLo(), getLo()) == 0 &&
               Double.compare(that.getHi(), getHi()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLo(), getHi());
    }

    public static SphereInterval fromPointPair(double a, double b) {
        assert Math.abs(a) <= Math.PI;
        assert Math.abs(b) <= Math.PI;
        if (a == -Math.PI) {
            a = Math.PI;
        }
        if (b == -Math.PI) {
            b = Math.PI;
        }
        if (positiveDistance(a, b) <= Math.PI) {
            return new SphereInterval(a, b, true);
        } else {
            return new SphereInterval(b, a, true);
        }
    }

    public static double positiveDistance(double a, double b) {
        double d = b - a;
        if (d >= 0) {
            return d;
        }
        return (b + Math.PI) - (a - Math.PI);
    }

    public static SphereInterval full() {
        return new SphereInterval(-Math.PI, Math.PI, true);
    }

    public boolean isFull() {
        return (getHi() - getLo()) == (2 * Math.PI);
    }

    public boolean isValid() {
        return Math.abs(getLo()) <= Math.PI &&
               Math.abs(getHi()) <= Math.PI &&
               !(getLo() == -Math.PI && getHi() != Math.PI) &&
               !(getHi() == -Math.PI && getLo() != Math.PI);
    }

    public boolean isInverted() {
        return getLo() > getHi();
    }

    public boolean isEmpty() {
        return getLo() - getHi() == 2 * Math.PI;
    }

    public double getCenter() {
        double center = 0.5 * (getLo() + getHi());
        if (!isInverted()) {
            return center;
        }
        if (center <= 0) {
            return center + Math.PI;
        } else {
            return center - Math.PI;
        }
    }

    public double getLength() {
        double length = getHi() - getLo();
        if (length >= 0) {
            return length;
        }
        length += (2 * Math.PI);
        if (length > 0) {
            return length;
        } else {
            return -1;
        }
    }

    public SphereInterval complement() {
        if (getLo() == getHi()) {
            return full();
        }
        return new SphereInterval(getHi(), getLo());
    }

    public boolean approxEquals(SphereInterval other, double maxError) {
        if (isEmpty()) {
            return other.getLength() <= maxError;
        }
        if (other.isEmpty()) {
            return getLength() <= maxError;
        }
        return ((Math.abs(drem(other.getLo() - getLo(), 2 * Math.PI)) +
                 Math.abs(drem(other.getHi() - getHi(), 2 * Math.PI))) <=
                maxError);
    }

    private static double drem(double x, double y) {
        // Implementation of double remainder
        return x % y;
    }

    public boolean fastContains(double other) {
        if (isInverted()) {
            return (other >= getLo() || other <= getHi()) && !isEmpty();
        } else {
            return other >= getLo() && other <= getHi();
        }
    }

    public boolean contains(SphereInterval other) {
        if (isInverted()) {
            if (other.isInverted()) {
                return other.getLo() >= getLo() && other.getHi() <= getHi();
            }
            return (other.getLo() >= getLo() || other.getHi() <= getHi()) && !isEmpty();
        } else {
            if (other.isInverted()) {
                return isFull() || other.isEmpty();
            }
            return other.getLo() >= getLo() && other.getHi() <= getHi();
        }
    }

    public boolean contains(double other) {
        assert Math.abs(other) <= Math.PI;
        if (other == -Math.PI) {
            other = Math.PI;
        }
        return fastContains(other);
    }

    public boolean interiorContains(SphereInterval other) {
        if (isInverted()) {
            if (!other.isInverted()) {
                return other.getLo() > getLo() || other.getHi() < getHi();
            }
            return (other.getLo() > getLo() && other.getHi() < getHi()) || other.isEmpty();
        } else {
            if (other.isInverted()) {
                return isFull() || other.isEmpty();
            }
            return (other.getLo() > getLo() && other.getHi() < getHi()) || isFull();
        }
    }

    public boolean interiorContains(double other) {
        assert Math.abs(other) <= Math.PI;
        if (other == -Math.PI) {
            other = Math.PI;
        }
        if (isInverted()) {
            return other > getLo() || other < getHi();
        } else {
            return (other > getLo() && other < getHi()) || isFull();
        }
    }

    public boolean intersects(SphereInterval other) {
        if (isEmpty() || other.isEmpty()) {
            return false;
        }
        if (isInverted()) {
            return other.isInverted() || other.getLo() <= getHi() || other.getHi() >= getLo();
        } else {
            if (other.isInverted()) {
                return other.getLo() <= getHi() || other.getHi() >= getLo();
            }
            return other.getLo() <= getHi() && other.getHi() >= getLo();
        }
    }

    public boolean interiorIntersects(SphereInterval other) {
        if (isEmpty() || other.isEmpty() || getLo() == getHi()) {
            return false;
        }
        if (isInverted()) {
            return other.isInverted() || other.getLo() < getHi() || other.getHi() > getLo();
        } else {
            if (other.isInverted()) {
                return other.getLo() < getHi() || other.getHi() > getLo();
            }
            return (other.getLo() < getHi() && other.getHi() > getLo()) || isFull();
        }
    }

    public SphereInterval union(SphereInterval other) {
        if (other.isEmpty()) {
            return this;
        }

        if (fastContains(other.getLo())) {
            if (fastContains(other.getHi())) {
                if (contains(other)) {
                    return this;
                }
                return full();
            }
            return new SphereInterval(getLo(), other.getHi(), true);
        }

        if (fastContains(other.getHi())) {
            return new SphereInterval(other.getLo(), getHi(), true);
        }

        if (isEmpty() || other.fastContains(getLo())) {
            return other;
        }

        double dlo = positiveDistance(other.getHi(), getLo());
        double dhi = positiveDistance(getHi(), other.getLo());
        if (dlo < dhi) {
            return new SphereInterval(other.getLo(), getHi(), true);
        } else {
            return new SphereInterval(getLo(), other.getHi(), true);
        }
    }

    public SphereInterval intersection(SphereInterval other) {
        if (other.isEmpty()) {
            return empty();
        }
        if (fastContains(other.getLo())) {
            if (fastContains(other.getHi())) {
                if (other.getLength() < getLength()) {
                    return other;
                }
                return this;
            }
            return new SphereInterval(other.getLo(), getHi(), true);
        }

        if (fastContains(other.getHi())) {
            return new SphereInterval(getLo(), other.getHi(), true);
        }

        if (other.fastContains(getLo())) {
            return this;
        }
        assert !intersects(other);
        return empty();
    }

    public SphereInterval expanded(double radius) {
        assert radius >= 0;
        if (isEmpty()) {
            return this;
        }

        if (getLength() + 2 * radius >= 2 * Math.PI - 1e-15) {
            return full();
        }

        double lo = drem(getLo() - radius, 2 * Math.PI);
        double hi = drem(getHi() + radius, 2 * Math.PI);
        if (lo <= -Math.PI) {
            lo = Math.PI;
        }
        return new SphereInterval(lo, hi);
    }

    public double getComplementCenter() {
        if (getLo() != getHi()) {
            return complement().getCenter();
        } else {
            if (getHi() <= 0) {
                return getHi() + Math.PI;
            } else {
                return getHi() - Math.PI;
            }
        }
    }

    public double getDirectedHausdorffDistance(SphereInterval other) {
        if (other.contains(this)) {
            return 0.0;
        }
        if (other.isEmpty()) {
            return Math.PI;
        }

        double otherComplementCenter = other.getComplementCenter();
        if (contains(otherComplementCenter)) {
            return positiveDistance(other.getHi(), otherComplementCenter);
        } else {
            double hiHi = 0;
            double loLo = 0;

            if (new SphereInterval(other.getHi(), otherComplementCenter).contains(getHi())) {
                hiHi = positiveDistance(other.getHi(), getHi());
            }
            if (new SphereInterval(otherComplementCenter, other.getLo()).contains(getLo())) {
                loLo = positiveDistance(getLo(), other.getLo());
            }

            assert hiHi > 0 || loLo > 0;
            return Math.max(hiHi, loLo);
        }
    }
}
