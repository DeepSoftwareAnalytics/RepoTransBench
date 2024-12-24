public class Matrix {
    private int[] vals;

    public Matrix(int... vals) {
        if (vals.length != 9) {
            throw new IllegalArgumentException("Matrix requires 9 values");
        }
        this.vals = vals;
    }

    public Matrix(int[][] vals) {
        if (vals.length != 3 || vals[0].length != 3 || vals[1].length != 3 || vals[2].length != 3) {
            throw new IllegalArgumentException("Matrix requires a 3x3 array");
        }
        this.vals = new int[9];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(vals[i], 0, this.vals, i * 3, 3);
        }
    }

    @Override
    public String toString() {
        return String.format("[%d, %d, %d,\n %d, %d, %d,\n %d, %d, %d]",
            vals[0], vals[1], vals[2], vals[3], vals[4], vals[5], vals[6], vals[7], vals[8]);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Matrix matrix = (Matrix) obj;
        return java.util.Arrays.equals(vals, matrix.vals);
    }

    public Matrix add(Matrix other) {
        int[] result = new int[9];
        for (int i = 0; i < 9; i++) {
            result[i] = this.vals[i] + other.vals[i];
        }
        return new Matrix(result);
    }

    public Matrix subtract(Matrix other) {
        int[] result = new int[9];
        for (int i = 0; i < 9; i++) {
            result[i] = this.vals[i] - other.vals[i];
        }
        return new Matrix(result);
    }

    public Point multiply(Point p) {
        int x = vals[0] * p.x + vals[1] * p.y + vals[2] * p.z;
        int y = vals[3] * p.x + vals[4] * p.y + vals[5] * p.z;
        int z = vals[6] * p.x + vals[7] * p.y + vals[8] * p.z;
        return new Point(x, y, z);
    }

    public Matrix multiply(Matrix other) {
        int[] result = new int[9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i * 3 + j] = 0;
                for (int k = 0; k < 3; k++) {
                    result[i * 3 + j] += this.vals[i * 3 + k] * other.vals[k * 3 + j];
                }
            }
        }
        return new Matrix(result);
    }

    public int[] getVals() {
        return this.vals;
    }
}
