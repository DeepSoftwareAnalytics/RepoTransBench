package com.example;

public class LatLon {
    private final Angle lat;
    private final Angle lon;

    private LatLon(Angle lat, Angle lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public static LatLon fromRadians(double latRadians, double lonRadians) {
        return new LatLon(Angle.fromRadians(latRadians), Angle.fromRadians(lonRadians));
    }

    public static LatLon fromDegrees(double latDegrees, double lonDegrees) {
        return new LatLon(Angle.fromDegrees(latDegrees), Angle.fromDegrees(lonDegrees));
    }

    public static LatLon invalid() {
        return new LatLon(null, null);
    }

    public Angle lat() {
        return lat;
    }

    public Angle lon() {
        return lon;
    }

    public boolean isValid() {
        return lat != null && lon != null 
            && lat.degrees() >= -90 && lat.degrees() <= 90
            && lon.degrees() >= -180 && lon.degrees() <= 180;
    }

    public LatLon normalized() {
        double normalizedLat = Math.max(-90, Math.min(90, lat.degrees()));
        double normalizedLon = ((lon.degrees() + 180) % 360 + 360) % 360 - 180;
        return LatLon.fromDegrees(normalizedLat, normalizedLon);
    }

    public boolean approxEquals(LatLon other) {
        return this.lat().degrees() == other.lat().degrees() &&
               this.lon().degrees() == other.lon().degrees();
    }

    public LatLon add(LatLon other) {
        return LatLon.fromDegrees(this.lat().degrees() + other.lat().degrees(), this.lon().degrees() + other.lon().degrees());
    }

    public LatLon subtract(LatLon other) {
        return LatLon.fromDegrees(this.lat().degrees() - other.lat().degrees(), this.lon().degrees() - other.lon().degrees());
    }

    public LatLon toPoint() {
        return this; // Dummy implementation for conversion methods
    }

    public static LatLon fromPoint(LatLon point) {
        return point; // Dummy implementation for conversion methods
    }

    public double distance(LatLon other) {
        // Simple Haversine formula for distance
        double dLat = Math.toRadians(other.lat.degrees() - this.lat.degrees());
        double dLon = Math.toRadians(other.lon.degrees() - this.lon.degrees());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(this.lat.degrees())) * Math.cos(Math.toRadians(other.lat.degrees())) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371.0 * c; // Earth radius in kilometers
    }

    public double getDistance(LatLon other) {
        return this.distance(other);
    }

    public static LatLon defaultLatLon() {
        return LatLon.fromDegrees(0, 0);
    }
}
