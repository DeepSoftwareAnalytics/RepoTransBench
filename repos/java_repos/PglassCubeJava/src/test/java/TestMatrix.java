import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestMatrix {
    @Test
    public void testMatrixFromItems() {
        Matrix A = new Matrix(1, 2, 3, 4, 5, 6, 7, 8, 9);
        assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9}, A.getVals());
    }

    @Test
    public void testMatrixFromList() {
        Matrix B = new Matrix(new int[][]{{9, 8, 7}, {6, 5, 4}, {3, 2, 1}});
        assertArrayEquals(new int[]{9, 8, 7, 6, 5, 4, 3, 2, 1}, B.getVals());
    }

    @Test
    public void testMatrixStr() {
        Matrix A = new Matrix(1, 2, 3, 4, 5, 6, 7, 8, 9);
        assertEquals("[1, 2, 3,\n 4, 5, 6,\n 7, 8, 9]", A.toString());
    }

    @Test
    public void testMatrixAdd() {
        Matrix A = new Matrix(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Matrix B = new Matrix(9, 8, 7, 6, 5, 4, 3, 2, 1);
        assertEquals(new Matrix(10, 10, 10, 10, 10, 10, 10, 10, 10), A.add(B));
    }

    @Test
    public void testMatrixSub() {
        Matrix A = new Matrix(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Matrix B = new Matrix(9, 8, 7, 6, 5, 4, 3, 2, 1);
        assertEquals(new Matrix(-8, -6, -4, -2, 0, 2, 4, 6, 8), A.subtract(B));
    }

    @Test
    public void testMatrixMul() {
        Matrix A = new Matrix(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Point p = new Point(1, 2, 3);
        assertEquals(new Point(14, 32, 50), A.multiply(p));
    }
}
