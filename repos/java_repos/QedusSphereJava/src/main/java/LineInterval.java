package com.example;

import java.util.Objects;
public class LineInterval extends Interval {

    public LineInterval() {
        super(1, 0);
    }

    public LineInterval(double lo, double hi) {
        super(lo, hi);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LineInterval that = (LineInterval) obj;
        return (this.getLo() == that.getLo() && this.getHi() == that.getHi()) ||
               (this.isEmpty() && that.isEmpty());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLo(), getHi());
    }

    public static LineInterval fromPointPair(double a, double b) {
        if (a <= b) {
            return new LineInterval(a, b);
        } else {
            return new LineInterval(b, a);
        }
    }

    public boolean contains(LineInterval other) {
        if (other.isEmpty()) {
            return true;
        }
        return other.getLo() >= this.getLo() && other.getHi() <= this.getHi();
    }

    public boolean contains(double point) {
        return point >= this.getLo() && point <= this.getHi();
    }

    public boolean interiorContains(LineInterval other) {
        if (other.isEmpty()) {
            return true;
        }
        return other.getLo() > this.getLo() && other.getHi() < this.getHi();
    }

    public boolean interiorContains(double point) {
        return point > this.getLo() && point < this.getHi();
    }

    public boolean intersects(LineInterval other) {
        if (this.getLo() <= other.getLo()) {
            return other.getLo() <= this.getHi() && other.getLo() <= other.getHi();
        } else {
            return this.getLo() <= other.getHi() && this.getLo() <= this.getHi();
        }
    }

    public boolean interiorIntersects(LineInterval other) {
        return other.getLo() < this.getHi() && this.getLo() < other.getHi() &&
               this.getLo() < this.getHi() && other.getLo() <= other.getHi();
    }

    public LineInterval union(LineInterval other) {
        if (this.isEmpty()) {
            return other;
        }
        if (other.isEmpty()) {
            return this;
        }
        return new LineInterval(Math.min(this.getLo(), other.getLo()), Math.max(this.getHi(), other.getHi()));
    }

    public LineInterval intersection(LineInterval other) {
        return new LineInterval(Math.max(this.getLo(), other.getLo()), Math.min(this.getHi(), other.getHi()));
    }

    public LineInterval expanded(double radius) {
        assert radius >= 0;
        if (this.isEmpty()) {
            return this;
        }
        return new LineInterval(this.getLo() - radius, this.getHi() + radius);
    }

    public double getCenter() {
        return 0.5 * (this.getLo() + this.getHi());
    }

    public double getLength() {
        return this.getHi() - this.getLo();
    }

    public boolean isEmpty() {
        return this.getLo() > this.getHi();
    }

    public boolean approxEquals(LineInterval other, double maxError) {
        if (this.isEmpty()) {
            return other.getLength() <= maxError;
        }
        if (other.isEmpty()) {
            return this.getLength() <= maxError;
        }
        return (Math.abs(other.getLo() - this.getLo()) +
                Math.abs(other.getHi() - this.getHi()) <= maxError);
    }
}
