package com.example;

public class Angle {
    private final double radians;

    public Angle() {
        this.radians = 0.0;
    }

    private Angle(double radians) {
        this.radians = radians;
    }

    public static Angle fromRadians(double radians) {
        return new Angle(radians);
    }

    public static Angle fromDegrees(double degrees) {
        return new Angle(Math.toRadians(degrees));
    }

    public double radians() {
        return radians;
    }

    public double degrees() {
        return Math.toDegrees(radians);
    }
}
