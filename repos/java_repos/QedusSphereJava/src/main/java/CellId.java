package com.example;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;

import com.example.LatLon;
import com.example.Point;
import com.example.AreaMetric;
import com.example.LengthMetric;
public class CellId {
    // Constants for CellId
    private static final int LOOKUP_BITS = 4;
    private static final int SWAP_MASK = 0x01;
    private static final int INVERT_MASK = 0x02;

    private static final int[][] POS_TO_IJ = {
            {0, 1, 3, 2},
            {0, 2, 3, 1},
            {3, 2, 0, 1},
            {3, 1, 0, 2}
    };

    private static final int[] POS_TO_ORIENTATION = {SWAP_MASK, 0, 0, INVERT_MASK | SWAP_MASK};
    private static final int[] LOOKUP_POS = new int[1 << (2 * LOOKUP_BITS + 2)];
    private static final int[] LOOKUP_IJ = new int[1 << (2 * LOOKUP_BITS + 2)];

    static {
        initLookupCell(0, 0, 0, 0, 0, 0);
        initLookupCell(0, 0, 0, SWAP_MASK, 0, SWAP_MASK);
        initLookupCell(0, 0, 0, INVERT_MASK, 0, INVERT_MASK);
        initLookupCell(0, 0, 0, SWAP_MASK | INVERT_MASK, 0, SWAP_MASK | INVERT_MASK);
    }

    private static void initLookupCell(int level, int i, int j, int origOrientation, int pos, int orientation) {
        if (level == LOOKUP_BITS) {
            int ij = (i << LOOKUP_BITS) + j;
            LOOKUP_POS[(ij << 2) + origOrientation] = (pos << 2) + orientation;
            LOOKUP_IJ[(pos << 2) + origOrientation] = (ij << 2) + orientation;
        } else {
            level++;
            i <<= 1;
            j <<= 1;
            pos <<= 2;
            int[] r = POS_TO_IJ[orientation];
            for (int index = 0; index < 4; index++) {
                initLookupCell(level, i + (r[index] >> 1), j + (r[index] & 1), origOrientation,
                        pos + index, orientation ^ POS_TO_ORIENTATION[index]);
            }
        }
    }

    // projection types
    public static final int LINEAR_PROJECTION = 0;
    public static final int TAN_PROJECTION = 1;
    public static final int QUADRATIC_PROJECTION = 2;

    // current projection used
    public static final int PROJECTION = QUADRATIC_PROJECTION;

    public static final int FACE_BITS = 3;
    public static final int NUM_FACES = 6;

    public static final int MAX_LEVEL = 30;
    public static final int POS_BITS = 2 * MAX_LEVEL + 1;
    public static final int MAX_SIZE = 1 << MAX_LEVEL;

    public static final long WRAP_OFFSET = NUM_FACES << POS_BITS;

    private long id;

    public CellId(long id) {
        this.id = id % 0xffffffffffffffffL;
    }

    @Override
    public String toString() {
        return "CellId: " + id();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof CellId)) return false;
        CellId cellId = (CellId) other;
        return id() == cellId.id();
    }

    public int compareTo(CellId other) {
        return Long.compare(id(), other.id());
    }

    public static CellId fromLatLon(LatLon ll) {
        return fromPoint(ll.toPoint());
    }

    public static CellId fromPoint(Point p) {
        int[] faceUv = xyzToFaceUv(p);
        int face = faceUv[0];
        double u = faceUv[1];
        double v = faceUv[2];
        int i = stToIj(uvToSt(u));
        int j = stToIj(uvToSt(v));
        return fromFaceIj(face, i, j);
    }

    public static CellId fromFacePosLevel(int face, int pos, int level) {
        return new CellId((face << POS_BITS) + (pos | 1)).parent(level);
    }

    public static CellId fromFaceIj(int face, int i, int j) {
        long n = face << (POS_BITS - 1);
        int bits = face & SWAP_MASK;

        for (int k = 7; k >= 0; k--) {
            int mask = (1 << LOOKUP_BITS) - 1;
            bits += (((i >> (k * LOOKUP_BITS)) & mask) << (LOOKUP_BITS + 2));
            bits += (((j >> (k * LOOKUP_BITS)) & mask) << 2);
            bits = LOOKUP_POS[bits];
            n |= (long) (bits >> 2) << (k * 2 * LOOKUP_BITS);
            bits &= (SWAP_MASK | INVERT_MASK);
        }

        return new CellId(n * 2 + 1);
    }

    public static CellId fromFaceIjWrap(int face, int i, int j) {
        i = Math.max(-1, Math.min(MAX_SIZE, i));
        j = Math.max(-1, Math.min(MAX_SIZE, j));

        double scale = 1.0 / MAX_SIZE;
        double u = scale * ((i << 1) + 1 - MAX_SIZE);
        double v = scale * ((j << 1) + 1 - MAX_SIZE);

        int[] faceUv = xyzToFaceUv(faceUvToXyz(face, u, v));
        face = faceUv[0];
        u = faceUv[1];
        v = faceUv[2];
        return fromFaceIj(face, stToIj(0.5 * (u + 1)), stToIj(0.5 * (v + 1)));
    }

    public static CellId fromFaceIjSame(int face, int i, int j, boolean sameFace) {
        if (sameFace) {
            return fromFaceIj(face, i, j);
        } else {
            return fromFaceIjWrap(face, i, j);
        }
    }

    public static int stToIj(double s) {
        return Math.max(0, Math.min(MAX_SIZE - 1, (int) Math.floor(MAX_SIZE * s)));
    }

    public static long lsbForLevel(int level) {
        return 1L << (2 * (MAX_LEVEL - level));
    }

    public CellId parent(int... args) {
        assert isValid();
        if (args.length == 0) {
            assert !isFace();
            long newLsb = lsb() << 2;
            return new CellId((id() & -newLsb) | newLsb);
        } else if (args.length == 1) {
            int level = args[0];
            assert level >= 0 && level <= level();
            long newLsb = lsbForLevel(level);
            return new CellId((id() & -newLsb) | newLsb);
        } else {
            throw new IllegalArgumentException("Invalid number of arguments for parent method");
        }
    }

    public CellId child(int pos) {
        assert isValid();
        assert !isLeaf();
        long newLsb = lsb() >> 2;
        return new CellId(id() + (2 * pos + 1 - 4) * newLsb);
    }

    public boolean contains(CellId other) {
        assert isValid();
        assert other.isValid();
        return other.compareTo(rangeMin()) >= 0 && other.compareTo(rangeMax()) <= 0;
    }

    public boolean intersects(CellId other) {
        assert isValid();
        assert other.isValid();
        return other.rangeMin().compareTo(rangeMax()) <= 0 && other.rangeMax().compareTo(rangeMin()) >= 0;
    }

    public boolean isFace() {
        return (id() & (lsbForLevel(0) - 1)) == 0;
    }

    public long id() {
        return this.id;
    }

    public boolean isValid() {
        return (face() < NUM_FACES) && ((lsb() & 0x1555555555555555L) != 0);
    }

    public long lsb() {
        return id() & -id();
    }

    public int face() {
        return (int) (id() >> POS_BITS);
    }

    public long pos() {
        return id() & (0xffffffffffffffffL >> FACE_BITS);
    }

    public boolean isLeaf() {
        return (id() & 1) != 0;
    }

    public int level() {
        if (isLeaf()) {
            return MAX_LEVEL;
        }

        long x = (id() & 0xffffffffL);
        int level = -1;
        if (x != 0) {
            level += 16;
        } else {
            x = ((id() >> 32) & 0xffffffffL);
        }

        x &= -x;
        if ((x & 0x00005555) != 0) level += 8;
        if ((x & 0x00550055) != 0) level += 4;
        if ((x & 0x05050505) != 0) level += 2;
        if ((x & 0x11111111) != 0) level += 1;

        assert level >= 0;
        assert level <= MAX_LEVEL;
        return level;
    }

    public CellId childBegin(int... args) {
        assert isValid();
        if (args.length == 0) {
            assert !isLeaf();

            long oldLsb = lsb();
            return new CellId(id() - oldLsb + (oldLsb >> 2));
        } else if (args.length == 1) {
            int level = args[0];
            assert level >= level() && level <= MAX_LEVEL;
            return new CellId(id() - lsb() + lsbForLevel(level));
        } else {
            throw new IllegalArgumentException("Invalid number of arguments for childBegin method");
        }
    }

    public CellId childEnd(int... args) {
        assert isValid();
        if (args.length == 0) {
            assert !isLeaf();
            long oldLsb = lsb();
            return new CellId(id() + oldLsb + (oldLsb >> 2));
        } else if (args.length == 1) {
            int level = args[0];
            assert level >= level() && level <= MAX_LEVEL;
            return new CellId(id() + lsb() + lsbForLevel(level));
        } else {
            throw new IllegalArgumentException("Invalid number of arguments for childEnd method");
        }
    }

    public CellId prev() {
        return new CellId(id() - (lsb() << 1));
    }

    public CellId next() {
        return new CellId(id() + (lsb() << 1));
    }

    public List<CellId> children(int... args) {
        List<CellId> result = new ArrayList<>();
        CellId cellId = childBegin(args);
        CellId end = childEnd(args);
        while (!cellId.equals(end)) {
            result.add(cellId);
            cellId = cellId.next();
        }
        return result;
    }

    public CellId rangeMin() {
        return new CellId(id() - (lsb() - 1));
    }

    public CellId rangeMax() {
        return new CellId(id() + (lsb() - 1));
    }

    public static CellId begin(int level) {
        return fromFacePosLevel(0, 0, 0).childBegin(level);
    }

    public static CellId end(int level) {
        return fromFacePosLevel(5, 0, 0).childEnd(level);
    }

    public static CellId none() {
        return new CellId(0);
    }

    public CellId prevWrap() {
        assert isValid();
        CellId p = prev();
        if (p.id() < WRAP_OFFSET) {
            return p;
        } else {
            return new CellId(p.id() + WRAP_OFFSET);
        }
    }

    public CellId nextWrap() {
        assert isValid();
        CellId n = next();
        if (n.id() < WRAP_OFFSET) {
            return n;
        } else {
            return new CellId(n.id() - WRAP_OFFSET);
        }
    }

    public CellId advanceWrap(int steps) {
        assert isValid();
        if (steps == 0) {
            return this;
        }

        int stepShift = 2 * (MAX_LEVEL - level()) + 1;
        if (steps < 0) {
            int minSteps = -(int) (id() >> stepShift);
            if (steps < minSteps) {
                int stepWrap = (int) (WRAP_OFFSET >> stepShift);
                steps %= stepWrap;
                if (steps < minSteps) {
                    steps += stepWrap;
                }
            }
        } else {
            int maxSteps = (int) ((WRAP_OFFSET - id()) >> stepShift);
            if (steps > maxSteps) {
                int stepWrap = (int) (WRAP_OFFSET >> stepShift);
                steps %= stepWrap;
                if (steps > maxSteps) {
                    steps -= stepWrap;
                }
            }
        }

        return new CellId(id() + (steps << stepShift));
    }

    public CellId advance(int steps) {
        if (steps == 0) {
            return this;
        }

        int stepShift = 2 * (MAX_LEVEL - level()) + 1;
        if (steps < 0) {
            int minSteps = -(int) (id() >> stepShift);
            if (steps < minSteps) {
                steps = minSteps;
            }
        } else {
            int maxSteps = (int) ((WRAP_OFFSET + lsb() - id()) >> stepShift);
            if (steps > maxSteps) {
                steps = maxSteps;
            }
        }

        return new CellId(id() + (steps << stepShift));
    }

    public LatLon toLatLon() {
        return LatLon.fromPoint(toPointRaw());
    }

    public Point toPointRaw() {
        int[] faceSiTi = getCenterSiTi();
        int face = faceSiTi[0];
        int si = faceSiTi[1];
        int ti = faceSiTi[2];
        return faceUvToXyz(face, stToUv((0.5 / MAX_SIZE) * si), stToUv((0.5 / MAX_SIZE) * ti));
    }

    public Point toPoint() {
        return toPointRaw().normalize();
    }

    public int[] getCenterSiTi() {
        int[] faceIjOrientation = toFaceIjOrientation();
        int face = faceIjOrientation[0];
        int i = faceIjOrientation[1];
        int j = faceIjOrientation[2];
        int orientation = faceIjOrientation[3];

        int delta;
        if (isLeaf()) {
            delta = 1;
        } else if (((i ^ (id() >> 2)) & 1) != 0) {
            delta = 2;
        } else {
            delta = 0;
        }

        return new int[]{face, 2 * i + delta, 2 * j + delta};
    }

    public double[] getCenterUv() {
        int[] faceSiTi = getCenterSiTi();
        int face = faceSiTi[0];
        int si = faceSiTi[1];
        int ti = faceSiTi[2];
        return new double[]{stToUv((0.5 / MAX_SIZE) * si), stToUv((0.5 / MAX_SIZE) * ti)};
    }

    public int[] toFaceIjOrientation() {
        int i = 0, j = 0;
        int face = face();
        int bits = face & SWAP_MASK;

        for (int k = 7; k >= 0; k--) {
            int nbits;
            if (k == 7) {
                nbits = MAX_LEVEL - 7 * LOOKUP_BITS;
            } else {
                nbits = LOOKUP_BITS;
            }

            bits += (id() >> (k * 2 * LOOKUP_BITS + 1) & ((1 << (2 * nbits)) - 1)) << 2;
            bits = LOOKUP_IJ[bits];
            i += (bits >> (LOOKUP_BITS + 2)) << (k * LOOKUP_BITS);
            j += ((bits >> 2) & ((1 << LOOKUP_BITS) - 1)) << (k * LOOKUP_BITS);
            bits &= (SWAP_MASK | INVERT_MASK);
        }

        assert POS_TO_ORIENTATION[2] == 0;
        assert POS_TO_ORIENTATION[0] == SWAP_MASK;
        if ((lsb() & 0x1111111111111110L) != 0) {
            bits ^= SWAP_MASK;
        }
        int orientation = bits;

        return new int[]{face, i, j, orientation};
    }

    public CellId[] getEdgeNeighbors() {
        int level = level();
        int size = getSizeIj(level);
        int[] faceIjOrientation = toFaceIjOrientation();
        int face = faceIjOrientation[0];
        int i = faceIjOrientation[1];
        int j = faceIjOrientation[2];

        return new CellId[]{
                fromFaceIjSame(face, i, j - size, j - size >= 0).parent(level),
                fromFaceIjSame(face, i + size, j, i + size < MAX_SIZE).parent(level),
                fromFaceIjSame(face, i, j + size, j + size < MAX_SIZE).parent(level),
                fromFaceIjSame(face, i - size, j, i - size >= 0).parent(level)
        };
    }

    public List<CellId> getVertexNeighbors(int level) {
        assert level < level();
        int[] faceIjOrientation = toFaceIjOrientation();
        int face = faceIjOrientation[0];
        int i = faceIjOrientation[1];
        int j = faceIjOrientation[2];

        int halfsize = getSizeIj(level + 1);
        int size = halfsize << 1;
        int ioffset, joffset;
        boolean isame, jsame;
        if ((i & halfsize) != 0) {
            ioffset = size;
            isame = (i + size) < MAX_SIZE;
        } else {
            ioffset = -size;
            isame = (i - size) >= 0;
        }
        if ((j & halfsize) != 0) {
            joffset = size;
            jsame = (j + size) < MAX_SIZE;
        } else {
            joffset = -size;
            jsame = (j - size) >= 0;
        }

        List<CellId> neighbors = new ArrayList<>();
        neighbors.add(parent(level));
        neighbors.add(fromFaceIjSame(face, i + ioffset, j, isame).parent(level));
        neighbors.add(fromFaceIjSame(face, i, j + joffset, jsame).parent(level));
        if (isame || jsame) {
            neighbors.add(fromFaceIjSame(face, i + ioffset, j + joffset, isame && jsame).parent(level));
        }

        return neighbors;
    }

    public List<CellId> getAllNeighbors(int nbrLevel) {
        int[] faceIjOrientation = toFaceIjOrientation();
        int face = faceIjOrientation[0];
        int i = faceIjOrientation[1];
        int j = faceIjOrientation[2];

        int size = getSizeIj();
        i &= -size;
        j &= -size;

        int nbrSize = getSizeIj(nbrLevel);
        assert nbrSize <= size;

        List<CellId> neighbors = new ArrayList<>();
        for (int k = -nbrSize; ; k += nbrSize) {
            boolean sameFace = k < 0 ? (j + k >= 0) : k >= size ? (j + k < MAX_SIZE) : false;
            neighbors.add(fromFaceIjSame(face, i + k, j - nbrSize, j - size >= 0).parent(nbrLevel));
            neighbors.add(fromFaceIjSame(face, i + k, j + size, j + size < MAX_SIZE).parent(nbrLevel));
            neighbors.add(fromFaceIjSame(face, i - nbrSize, j + k, sameFace && i - size >= 0).parent(nbrLevel));
            neighbors.add(fromFaceIjSame(face, i + size, j + k, sameFace && i + size < MAX_SIZE).parent(nbrLevel));

            if (k >= size) {
                break;
            }
        }

        return neighbors;
    }

    public int getSizeIj(int... args) {
        int level;
        if (args.length == 0) {
            level = level();
        } else {
            level = args[0];
        }
        return 1 << (MAX_LEVEL - level);
    }

    public String toToken() {
        return Long.toHexString(id());
    }

    public static CellId fromToken(String token) {
        return new CellId(Long.parseLong(token, 16));
    }

    public static double stToUv(double s) {
        switch (PROJECTION) {
            case LINEAR_PROJECTION:
                return 2 * s - 1;
            case TAN_PROJECTION:
                s = Math.tan((Math.PI / 2.0) * s - Math.PI / 4.0);
                return s + (1.0 / (1L << 53)) * s;
            case QUADRATIC_PROJECTION:
                return s >= 0.5 ? (1.0 / 3.0) * (4 * s * s - 1) : (1.0 / 3.0) * (1 - 4 * (1 - s) * (1 - s));
            default:
                throw new IllegalArgumentException("Unknown projection type");
        }
    }

    public static double uvToSt(double u) {
        switch (PROJECTION) {
            case LINEAR_PROJECTION:
                return 0.5 * (u + 1);
            case TAN_PROJECTION:
                return 2 * (1.0 / Math.PI) * (Math.atan(u) * Math.PI / 4.0);
            case QUADRATIC_PROJECTION:
                return u >= 0 ? 0.5 * Math.sqrt(1 + 3 * u) : 1 - 0.5 * Math.sqrt(1 - 3 * u);
            default:
                throw new IllegalArgumentException("Unknown projection type");
        }
    }

    public static AreaMetric avgArea() {
        return new AreaMetric(4 * Math.PI / 6);
    }

    public static LengthMetric maxEdge() {
        return new LengthMetric(maxAngleSpan().deriv());
    }

    public static LengthMetric maxAngleSpan() {
        switch (PROJECTION) {
            case LINEAR_PROJECTION:
                return new LengthMetric(2);
            case TAN_PROJECTION:
                return new LengthMetric(Math.PI / 2);
            case QUADRATIC_PROJECTION:
                return new LengthMetric(1.704897179199218452);
            default:
                throw new IllegalArgumentException("Unknown projection type");
        }
    }

    public static LengthMetric maxDiag() {
        switch (PROJECTION) {
            case LINEAR_PROJECTION:
                return new LengthMetric(2 * Math.sqrt(2));
            case TAN_PROJECTION:
                return new LengthMetric(Math.PI * Math.sqrt(2.0 / 3.0));
            case QUADRATIC_PROJECTION:
                return new LengthMetric(2.438654594434021032);
            default:
                throw new IllegalArgumentException("Unknown projection type");
        }
    }

    public static LengthMetric minWidth() {
        switch (PROJECTION) {
            case LINEAR_PROJECTION:
                return new LengthMetric(Math.sqrt(2));
            case TAN_PROJECTION:
                return new LengthMetric(Math.PI / 2 * Math.sqrt(2));
            case QUADRATIC_PROJECTION:
                return new LengthMetric(2 * Math.sqrt(2) / 3);
            default:
                throw new IllegalArgumentException("Unknown projection type");
        }
    }

    // Dummy implementations of xyzToFaceUv, uvToSt, LatLon, Point, AreaMetric, LengthMetric for compilation
    private static int[] xyzToFaceUv(Point p) {
        return new int[]{0, 0, 0};
    }

    private static Point faceUvToXyz(int face, double u, double v) {
        return new Point();
    }
}
