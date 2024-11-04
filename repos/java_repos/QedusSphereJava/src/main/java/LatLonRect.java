package com.example;

import java.util.Objects;

public class LatLonRect {
    private final LineInterval lat;
    private final SphereInterval lon;

    public LatLonRect() {
        this.lat = LineInterval.empty();
        this.lon = SphereInterval.empty();
    }

    public LatLonRect(LatLon lo, LatLon hi) {
        this.lat = new LineInterval(lo.getLat().getRadians(), hi.getLat().getRadians());
        this.lon = new SphereInterval(lo.getLon().getRadians(), hi.getLon().getRadians());
    }

    public LatLonRect(LineInterval lat, SphereInterval lon) {
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LatLonRect that = (LatLonRect) obj;
        return lat.equals(that.lat) && lon.equals(that.lon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lon);
    }

    @Override
    public String toString() {
        return "LatLonRect: " + lat + ", " + lon;
    }

    public LineInterval getLat() {
        return lat;
    }

    public SphereInterval getLon() {
        return lon;
    }

    public Angle getLatLo() {
        return Angle.fromRadians(lat.getLo());
    }

    public Angle getLatHi() {
        return Angle.fromRadians(lat.getHi());
    }

    public Angle getLonLo() {
        return Angle.fromRadians(lon.getLo());
    }

    public Angle getLonHi() {
        return Angle.fromRadians(lon.getHi());
    }

    public LatLon getLo() {
        return LatLon.fromAngles(getLatLo(), getLonLo());
    }

    public LatLon getHi() {
        return LatLon.fromAngles(getLatHi(), getLonHi());
    }

    public static LatLonRect fromCenterSize(LatLon center, LatLon size) {
        return fromPoint(center).expanded(size.multiply(0.5));
    }

    public static LatLonRect fromPoint(LatLon p) {
        assert p.isValid();
        return new LatLonRect(p, p);
    }

    public static LatLonRect fromPointPair(LatLon a, LatLon b) {
        assert a.isValid();
        assert b.isValid();
        return new LatLonRect(LineInterval.fromPointPair(a.getLat().getRadians(), b.getLat().getRadians()),
                              SphereInterval.fromPointPair(a.getLon().getRadians(), b.getLon().getRadians()));
    }

    public static LineInterval fullLat() {
        return new LineInterval(-Math.PI / 2.0, Math.PI / 2.0);
    }

    public static SphereInterval fullLon() {
        return SphereInterval.full();
    }

    public static LatLonRect full() {
        return new LatLonRect(fullLat(), fullLon());
    }

    public boolean isFull() {
        return lat.equals(fullLat()) && lon.isFull();
    }

    public boolean isValid() {
        return Math.abs(lat.getLo()) <= Math.PI / 2.0 &&
               Math.abs(lat.getHi()) <= Math.PI / 2.0 &&
               lon.isValid() &&
               lat.isEmpty() == lon.isEmpty();
    }

    public static LatLonRect empty() {
        return new LatLonRect();
    }

    public LatLon getCenter() {
        return LatLon.fromRadians(lat.getCenter(), lon.getCenter());
    }

    public LatLon getSize() {
        return LatLon.fromRadians(lat.getLength(), lon.getLength());
    }

    public LatLon getVertex(int k) {
        return LatLon.fromRadians(lat.bound(k >> 1), lon.bound((k >> 1) ^ (k & 1)));
    }

    public boolean isEmpty() {
        return lat.isEmpty();
    }

    public boolean isPoint() {
        return lat.getLo() == lat.getHi() && lon.getLo() == lon.getHi();
    }

    public LatLonRect convolveWithCap(Angle angle) {
        Cap cap = Cap.fromAxisAngle(new Point(1, 0, 0), angle);
        LatLonRect r = this;
        for (int k = 0; k < 4; k++) {
            Cap vertexCap = Cap.fromAxisHeight(getVertex(k).toPoint(), cap.getHeight());
            r = r.union(vertexCap.getRectBound());
        }
        return r;
    }

    public boolean contains(Object other) {
        if (other instanceof Point) {
            return contains(LatLon.fromPoint((Point) other));
        } else if (other instanceof LatLon) {
            LatLon latLon = (LatLon) other;
            assert latLon.isValid();
            return lat.contains(latLon.getLat().getRadians()) && lon.contains(latLon.getLon().getRadians());
        } else if (other instanceof LatLonRect) {
            LatLonRect otherRect = (LatLonRect) other;
            return lat.contains(otherRect.getLat()) && lon.contains(otherRect.getLon());
        } else if (other instanceof Cell) {
            return contains(((Cell) other).getRectBound());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public boolean interiorContains(Object other) {
        if (other instanceof Point) {
            return interiorContains(LatLon.fromPoint((Point) other));
        } else if (other instanceof LatLon) {
            LatLon latLon = (LatLon) other;
            assert latLon.isValid();
            return lat.interiorContains(latLon.getLat().getRadians()) && lon.interiorContains(latLon.getLon().getRadians());
        } else if (other instanceof LatLonRect) {
            LatLonRect otherRect = (LatLonRect) other;
            return lat.interiorContains(otherRect.getLat()) && lon.interiorContains(otherRect.getLon());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public boolean mayIntersect(Cell cell) {
        return intersects(cell.getRectBound());
    }

    public boolean intersects(Object... args) {
        if (args[0] instanceof LatLonRect) {
            LatLonRect otherRect = (LatLonRect) args[0];
            return lat.intersects(otherRect.getLat()) && lon.intersects(otherRect.getLon());
        } else if (args[0] instanceof Cell) {
            Cell cell = (Cell) args[0];
            if (isEmpty()) return false;
            if (contains(cell.getCenterRaw())) return true;
            if (cell.contains(getCenter().toPoint())) return true;
            if (!intersects(cell.getRectBound())) return false;

            Point[] cellVertices = new Point[4];
            LatLon[] cellLatLons = new LatLon[4];
            for (int i = 0; i < 4; i++) {
                cellVertices[i] = cell.getVertex(i);
                cellLatLons[i] = LatLon.fromPoint(cellVertices[i]);
                if (contains(cellLatLons[i])) return true;
                if (cell.contains(getVertex(i).toPoint())) return true;
            }

            for (int i = 0; i < 4; i++) {
                SphereInterval edgeLon = SphereInterval.fromPointPair(
                        cellLatLons[i].getLon().getRadians(),
                        cellLatLons[(i + 1) & 3].getLon().getRadians());
                if (!lon.intersects(edgeLon)) continue;

                Point a = cellVertices[i];
                Point b = cellVertices[(i + 1) & 3];
                if (edgeLon.contains(lon.getLo())) {
                    if (intersectsLonEdge(a, b, lat, lon.getLo())) return true;
                }
                if (edgeLon.contains(lon.getHi())) {
                    if (intersectsLonEdge(a, b, lat, lon.getHi())) return true;
                }
                if (intersectsLatEdge(a, b, lat.getLo(), lon)) return true;
                if (intersectsLatEdge(a, b, lat.getHi(), lon)) return true;
            }
            return false;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public boolean interiorIntersects(Object... args) {
        if (args[0] instanceof LatLonRect) {
            LatLonRect otherRect = (LatLonRect) args[0];
            return lat.interiorIntersects(otherRect.getLat()) && lon.interiorIntersects(otherRect.getLon());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public LatLonRect union(LatLonRect other) {
        return new LatLonRect(lat.union(other.getLat()), lon.union(other.getLon()));
    }

    public LatLonRect intersection(LatLonRect other) {
        LineInterval latIntersection = lat.intersection(other.getLat());
        SphereInterval lonIntersection = lon.intersection(other.getLon());
        if (latIntersection.isEmpty() || lonIntersection.isEmpty()) {
            return empty();
        }
        return new LatLonRect(latIntersection, lonIntersection);
    }

    public LatLonRect expanded(LatLon margin) {
        assert margin.getLat().getRadians() > 0;
        assert margin.getLon().getRadians() > 0;
        return new LatLonRect(
            lat.expanded(margin.getLat().getRadians()).intersection(fullLat()),
            lon.expanded(margin.getLon().getRadians())
        );
    }

    public boolean approxEquals(LatLonRect other, double maxError) {
        return lat.approxEquals(other.getLat(), maxError) && lon.approxEquals(other.getLon(), maxError);
    }

    public Cap getCapBound() {
        if (isEmpty()) {
            return Cap.empty();
        }

        double poleZ;
        double poleAngle;
        if (lat.getLo() + lat.getHi() < 0) {
            poleZ = -1;
            poleAngle = Math.PI / 2.0 + lat.getHi();
        } else {
            poleZ = 1;
            poleAngle = Math.PI / 2.0 - lat.getLo();
        }

        Cap poleCap = Cap.fromAxisAngle(new Point(0, 0, poleZ), Angle.fromRadians(poleAngle));
        double lonSpan = lon.getHi() - lon.getLo();
        if (Math.IEEEremainder(lonSpan, 2 * Math.PI) >= 0) {
            if (lonSpan < 2 * Math.PI) {
                Cap midCap = Cap.fromAxisAngle(getCenter().toPoint(), Angle.fromRadians(0));
                for (int k = 0; k < 4; k++) {
                    midCap.addPoint(getVertex(k).toPoint());
                }
                if (midCap.getHeight() < poleCap.getHeight()) {
                    return midCap;
                }
            }
        }
        return poleCap;
    }

    private static boolean intersectsLonEdge(Point a, Point b, LineInterval lat, double lon) {
        return simpleCrossing(a, b,
                LatLon.fromRadians(lat.getLo(), lon).toPoint(),
                LatLon.fromRadians(lat.getHi(), lon).toPoint());
    }

    private static boolean intersectsLatEdge(Point a, Point b, double lat, SphereInterval lon) {
        assert isUnitLength(a);
        assert isUnitLength(b);

        Point z = robustCrossProd(a, b).normalize();
        if (z.get(2) < 0) {
            z = z.negate();
        }

        Point y = robustCrossProd(z, new Point(0, 0, 1)).normalize();
        Point x = y.crossProduct(z);
        assert isUnitLength(x);
        assert x.get(2) >= 0;

        double sinLat = Math.sin(lat);
        if (Math.abs(sinLat) >= x.get(2)) {
            return false;
        }

        double cosTheta = sinLat / x.get(2);
        double sinTheta = Math.sqrt(1 - cosTheta * cosTheta);
        double theta = Math.atan2(sinTheta, cosTheta);

        SphereInterval abTheta = SphereInterval.fromPointPair(
                Math.atan2(a.dotProduct(y), a.dotProduct(x)),
                Math.atan2(b.dotProduct(y), b.dotProduct(x)));

        if (abTheta.contains(theta)) {
            Point isect = x.multiply(cosTheta).add(y.multiply(sinTheta));
            if (lon.contains(Math.atan2(isect.get(1), isect.get(0)))) {
                return true;
            }
        }
        if (abTheta.contains(-theta)) {
            Point isect = x.multiply(cosTheta).subtract(y.multiply(sinTheta));
            if (lon.contains(Math.atan2(isect.get(1), isect.get(0)))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isUnitLength(Point point) {
        // Implement the logic to check if a point is of unit length
        return true; // This is a placeholder. Implement actual logic here.
    }

    private static boolean simpleCrossing(Point a, Point b, Point c, Point d) {
        // Implement the logic for simple crossing
        return true; // This is a placeholder. Implement actual logic here.
    }

    private static Point robustCrossProd(Point a, Point b) {
        // Implement the logic for robust cross product
        return new Point(0, 0, 0); // This is a placeholder. Implement actual logic here.
    }
}
