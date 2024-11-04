package com.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.example.CellId;
import com.example.Point;
import com.example.Cap;
import com.example.LatLonRect;
public class Cell {

    private CellId cellId;
    private double[][] uv = new double[2][2];
    private int face;
    private int orientation;
    private int level;

    public Cell() {
    }

    public Cell(CellId cellId) {
        this.cellId = cellId;
        int[] faceIjOrientation = cellId.toFaceIjOrientation();
        this.face = faceIjOrientation[0];
        int i = faceIjOrientation[1];
        int j = faceIjOrientation[2];
        this.orientation = faceIjOrientation[3];
        this.level = cellId.level();
        int cellSize = cellId.getSizeIj();
        for (int d = 0; d < 2; d++) {
            int ijLo = i & -cellSize;
            int ijHi = ijLo + cellSize;
            this.uv[d][0] = CellId.stToUv((1.0 / CellId.MAX_SIZE) * ijLo);
            this.uv[d][1] = CellId.stToUv((1.0 / CellId.MAX_SIZE) * ijHi);
        }
    }

    public static Cell fromLatLon(LatLon latLon) {
        return new Cell(CellId.fromLatLon(latLon));
    }

    public static Cell fromPoint(Point point) {
        return new Cell(CellId.fromPoint(point));
    }

    @Override
    public String toString() {
        return String.format("%s: face %d, level %d, orientation %d, id %s",
                this.getClass().getSimpleName(), face, level, orientation(), cellId.id());
    }

    public static Cell fromFacePosLevel(int face, long pos, int level) {
        return new Cell(CellId.fromFacePosLevel(face, pos, level));
    }

    public CellId getId() {
        return cellId;
    }

    public int getFace() {
        return face;
    }

    public int getLevel() {
        return level;
    }

    public int orientation() {
        return orientation;
    }

    public boolean isLeaf() {
        return level == CellId.MAX_LEVEL;
    }

    public Point getEdge(int k) {
        return getEdgeRaw(k).normalize();
    }

    public Point getEdgeRaw(int k) {
        switch (k) {
            case 0:
                return getVNorm(face, uv[1][0]); // South
            case 1:
                return getUNorm(face, uv[0][1]); // East
            case 2:
                return getVNorm(face, uv[1][1]).negate(); // North
            case 3:
                return getUNorm(face, uv[0][0]).negate(); // West
            default:
                throw new IllegalArgumentException("Invalid edge index: " + k);
        }
    }

    public Point getVertex(int k) {
        return getVertexRaw(k).normalize();
    }

    public Point getVertexRaw(int k) {
        return faceUvToXyz(face, uv[0][(k >> 1) ^ (k & 1)], uv[1][k >> 1]);
    }

    public double exactArea() {
        Point v0 = getVertex(0);
        Point v1 = getVertex(1);
        Point v2 = getVertex(2);
        Point v3 = getVertex(3);
        return area(v0, v1, v2) + area(v0, v2, v3);
    }

    public double averageArea() {
        return CellId.avgArea().getValue(level);
    }

    public double approxArea() {
        if (level < 2) {
            return averageArea();
        }

        double flatArea = 0.5 * getVertex(2).subtract(getVertex(0))
                .crossProduct(getVertex(3).subtract(getVertex(1))).norm();

        return flatArea * 2 / (1 + Math.sqrt(1 - Math.min((1.0 / Math.PI) * flatArea, 1.0)));
    }

    public List<Cell> subdivide() {
        List<Cell> children = new ArrayList<>(4);
        double[] uvMid = cellId.getCenterUv();
    
        for (int pos = 0; pos < 4; pos++) {
            Cell child = new Cell();
            child.face = face;
            child.level = level + 1;
            // Assuming POS_TO_ORIENTATION is a 2D array
            child.orientation = orientation ^ POS_TO_ORIENTATION[orientation][pos];
            child.cellId = cellId.children().get(pos);
    
            int ij = POS_TO_IJ[orientation][pos];
            int i = ij >> 1;
            int j = ij & 1;
            child.uv[0][i] = uv[0][i];
            child.uv[0][1 - i] = uvMid[0];
            child.uv[1][j] = uv[1][j];
            child.uv[1][1 - j] = uvMid[1];
            children.add(child);
        }
    
        return children;
    }
    public Point getCenter() {
        return getCenterRaw().normalize();
    }

    public Point getCenterRaw() {
        return cellId.toPointRaw();
    }

    public boolean contains(Object other) {
        if (other instanceof Cell) {
            return cellId.contains(((Cell) other).cellId);
        } else if (other instanceof Point) {
            Point point = (Point) other;
            double[] uv = faceXyzToUv(face, point);
            return uv[0] >= this.uv[0][0] && uv[0] <= this.uv[0][1] &&
                   uv[1] >= this.uv[1][0] && uv[1] <= this.uv[1][1];
        }
        return false;
    }

    public boolean mayIntersect(Cell cell) {
        return cellId.intersects(cell.cellId);
    }

    public double getLatitude(int i, int j) {
        Point p = faceUvToXyz(face, uv[0][i], uv[1][j]);
        return LatLon.latitude(p).getRadians();
    }

    public double getLongitude(int i, int j) {
        Point p = faceUvToXyz(face, uv[0][i], uv[1][j]);
        return LatLon.longitude(p).getRadians();
    }

    public Cap getCapBound() {
        double u = 0.5 * (uv[0][0] + uv[0][1]);
        double v = 0.5 * (uv[1][0] + uv[1][1]);
        Cap cap = Cap.fromAxisHeight(faceUvToXyz(face, u, v).normalize(), 0);
        for (int k = 0; k < 4; k++) {
            cap.addPoint(getVertex(k));
        }
        return cap;
    }

    public LatLonRect getRectBound() {
        if (level > 0) {
            double u = uv[0][0] + uv[0][1];
            double v = uv[1][0] + uv[1][1];
            int i = (getUAxis(face)[2] == 0) ? (u < 0 ? 1 : 0) : (u > 0 ? 1 : 0);
            int j = (getVAxis(face)[2] == 0) ? (v < 0 ? 1 : 0) : (v > 0 ? 1 : 0);

            double maxError = 1.0 / (1 << 51);
            LineInterval lat = LineInterval.fromPointPair(getLatitude(i, j), getLatitude(1 - i, 1 - j))
                    .expanded(maxError).intersection(LatLonRect.fullLat());

            if (lat.getLo() == -Math.PI / 2.0 || lat.getHi() == Math.PI / 2.0) {
                return new LatLonRect(lat, SphereInterval.full());
            }

            SphereInterval lon = SphereInterval.fromPointPair(getLongitude(i, 1 - j), getLongitude(1 - i, j))
                    .expanded(maxError);
            return new LatLonRect(lat, lon);
        }

        double poleMinLat = Math.asin(Math.sqrt(1.0 / 3.0));

        switch (face) {
            case 0:
                return new LatLonRect(new LineInterval(-Math.PI / 4.0, Math.PI / 4.0),
                        new SphereInterval(-Math.PI / 4.0, Math.PI / 4.0));
            case 1:
                return new LatLonRect(new LineInterval(-Math.PI / 4.0, Math.PI / 4.0),
                        new SphereInterval(Math.PI / 4.0, 3.0 * Math.PI / 4.0));
            case 2:
                return new LatLonRect(new LineInterval(poleMinLat, Math.PI / 2.0),
                        new SphereInterval(-Math.PI, Math.PI));
            case 3:
                return new LatLonRect(new LineInterval(-Math.PI / 4.0, Math.PI / 4.0),
                        new SphereInterval(3.0 * Math.PI / 4.0, -3.0 * Math.PI / 4.0));
            case 4:
                return new LatLonRect(new LineInterval(-Math.PI / 4.0, Math.PI / 4.0),
                        new SphereInterval(-3.0 * Math.PI / 4.0, -Math.PI / 4.0));
            case 5:
                return new LatLonRect(new LineInterval(-Math.PI / 2.0, -poleMinLat),
                        new SphereInterval(-Math.PI, Math.PI));
            default:
                throw new IllegalArgumentException("Invalid face index: " + face);
        }
    }

    private Point getVNorm(int face, double v) {
        // Implement the logic to get the V norm
        return new Point(0, 0, 0); // Placeholder, replace with actual implementation
    }

    private Point getUNorm(int face, double u) {
        // Implement the logic to get the U norm
        return new Point(0, 0, 0); // Placeholder, replace with actual implementation
    }

    private Point faceUvToXyz(int face, double u, double v) {
        // Implement the logic to convert face UV to XYZ coordinates
        return new Point(0, 0, 0); // Placeholder, replace with actual implementation
    }

    private double[] faceXyzToUv(int face, Point p) {
        // Implement the logic to convert face XYZ to UV coordinates
        return new double[]{0, 0}; // Placeholder, replace with actual implementation
    }

    private double[] getUAxis(int face) {
        // Implement the logic to get U axis
        return new double[]{0, 0, 0}; // Placeholder, replace with actual implementation
    }

    private double[] getVAxis(int face) {
        // Implement the logic to get V axis
        return new double[]{0, 0, 0}; // Placeholder, replace with actual implementation
    }

    private double area(Point v0, Point v1, Point v2) {
        // Implement the logic to calculate area
        return 0.0; // Placeholder, replace with actual implementation
    }

    private static final int[][] POS_TO_ORIENTATION = {
        {0, 1, 2, 3},
        {0, 1, 2, 3},
        {0, 1, 2, 3},
        {0, 1, 2, 3},
        {0, 1, 2, 3},
        {0, 1, 2, 3}
    };

    private static final int[][] POS_TO_IJ = {
        {0, 1, 2, 3},
        {0, 1, 2, 3},
        {0, 1, 2, 3},
        {0, 1, 2, 3},
        {0, 1, 2, 3},
        {0, 1, 2, 3}
    };
}
