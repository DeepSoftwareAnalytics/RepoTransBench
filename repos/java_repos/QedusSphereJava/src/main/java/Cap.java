package com.example;

import java.util.Objects;

public class Cap {
    private static final double ROUND_UP = 1.0 + 1.0 / (1 << 52);
    private Point axis;
    private double height;

    public Cap() {
        this.axis = new Point(1, 0, 0);
        this.height = -1;
    }

    public Cap(Point axis, double height) {
        this.axis = axis;
        this.height = height;
    }

    @Override
    public String toString() {
        return "Cap: " + axis + " " + height;
    }

    public static Cap fromAxisHeight(Point axis, double height) {
        assert isUnitLength(axis);
        return new Cap(axis, height);
    }

    public static Cap fromAxisAngle(Point axis, Angle angle) {
        assert isUnitLength(axis);
        assert angle.getRadians() >= 0;
        return new Cap(axis, getHeightForAngle(angle.getRadians()));
    }

    public static double getHeightForAngle(double radians) {
        assert radians >= 0;
        if (radians >= Math.PI) {
            return 2;
        }
        double d = Math.sin(0.5 * radians);
        return 2 * d * d;
    }

    public static Cap fromAxisArea(Point axis, double area) {
        assert isUnitLength(axis);
        return new Cap(axis, area / (2 * Math.PI));
    }

    public static Cap empty() {
        return new Cap();
    }

    public static Cap full() {
        return new Cap(new Point(1, 0, 0), 2);
    }

    public double getHeight() {
        return height;
    }

    public Point getAxis() {
        return axis;
    }

    public double getArea() {
        return 2 * Math.PI * Math.max(0.0, getHeight());
    }

    public Angle getAngle() {
        if (isEmpty()) {
            return Angle.fromRadians(-1);
        }
        return Angle.fromRadians(2 * Math.asin(Math.sqrt(0.5 * getHeight())));
    }

    public boolean isValid() {
        return isUnitLength(getAxis()) && getHeight() <= 2;
    }

    public boolean isEmpty() {
        return getHeight() < 0;
    }

    public boolean isFull() {
        return getHeight() >= 2;
    }

    public Cap getCapBound() {
        return this;
    }

    public void addPoint(Point point) {
        assert isUnitLength(point);
        if (isEmpty()) {
            this.axis = point;
            this.height = 0;
        } else {
            double dist2 = this.axis.subtract(point).norm2();
            this.height = Math.max(this.height, ROUND_UP * 0.5 * dist2);
        }
    }

    public Cap complement() {
        double newHeight = isFull() ? -1 : 2 - Math.max(getHeight(), 0.0);
        return fromAxisHeight(this.axis.negate(), newHeight);
    }

    public boolean contains(Object other) {
        if (other instanceof Cap) {
            Cap otherCap = (Cap) other;
            if (isFull() || otherCap.isEmpty()) {
                return true;
            }
            return getAngle().getRadians() >= this.axis.angle(otherCap.axis).getRadians() + otherCap.getAngle().getRadians();
        } else if (other instanceof Point) {
            Point point = (Point) other;
            assert isUnitLength(point);
            return this.axis.subtract(point).norm2() <= 2 * getHeight();
        } else if (other instanceof Cell) {
            Cell cell = (Cell) other;
            for (int k = 0; k < 4; k++) {
                if (!this.contains(cell.getVertex(k))) {
                    return false;
                }
            }
            return !this.complement().intersects(cell, cell.getVertices());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public boolean interiorContains(Point point) {
        assert isUnitLength(point);
        return isFull() || this.axis.subtract(point).norm2() < 2 * getHeight();
    }

    public boolean intersects(Object... args) {
        if (args.length == 1 && args[0] instanceof Cap) {
            Cap otherCap = (Cap) args[0];
            if (isEmpty() || otherCap.isEmpty()) {
                return false;
            }
            return getAngle().getRadians() + otherCap.getAngle().getRadians() >= this.axis.angle(otherCap.axis).getRadians();
        } else if (args.length == 2 && args[0] instanceof Cell && args[1] instanceof Point[]) {
            Cell cell = (Cell) args[0];
            Point[] vertices = (Point[]) args[1];
            if (getHeight() >= 1 || isEmpty()) {
                return false;
            }
            if (cell.contains(this.axis)) {
                return true;
            }

            double sin2Angle = getHeight() * (2 - getHeight());
            for (int k = 0; k < 4; k++) {
                Point edge = cell.getEdgeRaw(k);
                double dot = this.axis.dotProduct(edge);
                if (dot > 0) {
                    continue;
                }
                if (dot * dot > sin2Angle * edge.norm2()) {
                    return false;
                }
                Point dir = edge.crossProduct(this.axis);
                if (dir.dotProduct(vertices[k]) < 0 && dir.dotProduct(vertices[(k + 1) & 3]) > 0) {
                    return true;
                }
            }
            return false;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public boolean mayIntersect(Cell cell) {
        for (int k = 0; k < 4; k++) {
            if (this.contains(cell.getVertex(k))) {
                return true;
            }
        }
        return this.intersects(cell, cell.getVertices());
    }

    public boolean interiorIntersects(Cap otherCap) {
        if (getHeight() <= 0 || otherCap.isEmpty()) {
            return false;
        }
        return getAngle().getRadians() + otherCap.getAngle().getRadians() > this.axis.angle(otherCap.axis).getRadians();
    }

    public LatLonRect getRectBound() {
        if (isEmpty()) {
            return LatLonRect.empty();
        }

        LatLon axisLL = LatLon.fromPoint(this.axis);
        double capAngle = getAngle().getRadians();

        boolean allLongitudes = false;
        double[] lat = new double[2];
        double[] lon = new double[2];
        lon[0] = -Math.PI;
        lon[1] = Math.PI;

        lat[0] = axisLL.getLat().getRadians() - capAngle;
        if (lat[0] <= -Math.PI / 2.0) {
            lat[0] = -Math.PI / 2.0;
            allLongitudes = true;
        }

        lat[1] = axisLL.getLat().getRadians() + capAngle;
        if (lat[1] >= Math.PI / 2.0) {
            lat[1] = Math.PI / 2.0;
            allLongitudes = true;
        }

        if (!allLongitudes) {
            double sinA = Math.sqrt(getHeight() * (2 - getHeight()));
            double sinC = Math.cos(axisLL.getLat().getRadians());
            if (sinA <= sinC) {
                double angleA = Math.asin(sinA / sinC);
                lon[0] = drem(axisLL.getLon().getRadians() - angleA, 2 * Math.PI);
                lon[1] = drem(axisLL.getLon().getRadians() + angleA, 2 * Math.PI);
            }
        }

        return new LatLonRect(new LineInterval(lat[0], lat[1]), new SphereInterval(lon[0], lon[1]));
    }

    public boolean approxEquals(Cap otherCap, double maxError) {
        return (this.axis.angle(otherCap.axis).getRadians() <= maxError && Math.abs(this.height - otherCap.height) <= maxError) ||
               (this.isEmpty() && otherCap.getHeight() <= maxError) ||
               (otherCap.isEmpty() && this.getHeight() <= maxError) ||
               (this.isFull() && otherCap.getHeight() >= 2 - maxError) ||
               (otherCap.isFull() && this.getHeight() >= 2 - maxError);
    }

    public Cap expanded(Angle distance) {
        assert distance.getRadians() >= 0;
        if (isEmpty()) {
            return Cap.empty();
        }
        return Cap.fromAxisAngle(this.axis, this.getAngle().add(distance));
    }

    private static boolean isUnitLength(Point point) {
        // Implement the logic to check if a point is of unit length
        return true; // This is a placeholder. Implement actual logic here.
    }

    private static double drem(double x, double y) {
        // Implement the logic for double remainder
        return x % y; // This is a placeholder. Implement actual logic here.
    }
}
