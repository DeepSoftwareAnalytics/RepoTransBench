public class Point {
    public int x, y, z;

    public Point(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point(int[] coords) {
        if (coords.length != 3) {
            throw new IllegalArgumentException("Point requires 3 coordinates");
        }
        this.x = coords[0];
        this.y = coords[1];
        this.z = coords[2];
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Point point = (Point) obj;
        return x == point.x && y == point.y && z == point.z;
    }

    public Point add(Point other) {
        return new Point(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Point subtract(Point other) {
        return new Point(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Point scale(int scalar) {
        return new Point(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public int dot(Point other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public Point cross(Point other) {
        return new Point(
            this.y * other.z - this.z * other.y,
            this.z * other.x - this.x * other.z,
            this.x * other.y - this.y * other.x
        );
    }

    public int count(int val) {
        return (this.x == val ? 1 : 0) + (this.y == val ? 1 : 0) + (this.z == val ? 1 : 0);
    }
}
