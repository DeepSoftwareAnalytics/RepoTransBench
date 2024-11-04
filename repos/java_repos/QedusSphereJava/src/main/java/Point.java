package com.example;

import java.util.Objects;

public class Point {
    private final double x;
    private final double y;
    private final double z;

    public Point(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double get(int index) {
        switch (index) {
            case 0: return x;
            case 1: return y;
            case 2: return z;
            default: throw new IndexOutOfBoundsException("Invalid index: " + index);
        }
    }

    public Point negate() {
        return new Point(-x, -y, -z);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Point point = (Point) obj;
        return Double.compare(point.x, x) == 0 &&
               Double.compare(point.y, y) == 0 &&
               Double.compare(point.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "Point: (" + x + ", " + y + ", " + z + ")";
    }

    public Point add(Point other) {
        return new Point(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Point subtract(Point other) {
        return new Point(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Point multiply(double scalar) {
        return new Point(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public Point abs() {
        return new Point(Math.abs(x), Math.abs(y), Math.abs(z));
    }

    public int largestAbsComponent() {
        Point temp = this.abs();
        if (temp.x > temp.y) {
            return temp.x > temp.z ? 0 : 2;
        } else {
            return temp.y > temp.z ? 1 : 2;
        }
    }

    public double angle(Point other) {
        return Math.atan2(this.crossProduct(other).norm(), this.dotProduct(other));
    }

    public Point crossProduct(Point other) {
        return new Point(
            this.y * other.z - this.z * other.y,
            this.z * other.x - this.x * other.z,
            this.x * other.y - this.y * other.x
        );
    }

    public double dotProduct(Point other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public double norm2() {
        return x * x + y * y + z * z;
    }

    public double norm() {
        return Math.sqrt(norm2());
    }

    public Point normalize() {
        double n = norm();
        if (n != 0) {
            n = 1.0 / n;
        }
        return new Point(this.x * n, this.y * n, this.z * n);
    }
}
