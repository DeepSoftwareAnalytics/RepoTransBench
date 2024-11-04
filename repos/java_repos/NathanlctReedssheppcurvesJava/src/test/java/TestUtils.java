import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.Math;
import utils.Utils;

public class TestUtils {

    @Test
    public void test_M() {
        assertEquals(-Math.PI, Utils.M(Math.PI), 1e-6);
        assertEquals(-Math.PI, Utils.M(-Math.PI), 1e-6);
        assertEquals(0, Utils.M(2 * Math.PI), 1e-6);
        assertEquals(0, Utils.M(-2 * Math.PI), 1e-6);
        assertEquals(Math.PI / 2, Utils.M(Math.PI / 2), 1e-6);
        assertEquals(-Math.PI / 2, Utils.M(-Math.PI / 2), 1e-6);
    }

    @Test
    public void test_R() {
        assertArrayEquals(new double[]{1, 0}, Utils.R(1, 0), 1e-6);
        assertArrayEquals(new double[]{1, Math.PI / 2}, Utils.R(0, 1), 1e-6);
        assertArrayEquals(new double[]{1, Math.PI}, Utils.R(-1, 0), 1e-6);
        assertArrayEquals(new double[]{1, -Math.PI / 2}, Utils.R(0, -1), 1e-6);
        assertArrayEquals(new double[]{Math.sqrt(2), Math.PI / 4}, Utils.R(1, 1), 1e-6);
        assertArrayEquals(new double[]{Math.sqrt(2), -3 * Math.PI / 4}, Utils.R(-1, -1), 1e-6);
    }

    @Test
    public void test_change_of_basis() {
        assertArrayEquals(new double[]{1, 0, 0}, Utils.changeOfBasis(new double[]{0, 0, 0}, new double[]{1, 0, 0}), 1e-6);
        assertArrayEquals(new double[]{0, 1, 0}, Utils.changeOfBasis(new double[]{0, 0, 0}, new double[]{0, 1, 0}), 1e-6);
        assertArrayEquals(new double[]{0, 0, 90}, Utils.changeOfBasis(new double[]{0, 0, 0}, new double[]{0, 0, 90}), 1e-6);
        assertArrayEquals(new double[]{1, 1, 0}, Utils.changeOfBasis(new double[]{1, 1, 0}, new double[]{2, 2, 0}), 1e-6);
        assertEquals(Math.sqrt(2), Utils.changeOfBasis(new double[]{1, 1, 45}, new double[]{2, 2, 45})[0], 1e-6);
        assertEquals(0, Utils.changeOfBasis(new double[]{1, 1, 45}, new double[]{2, 2, 45})[1], 1e-6);
        assertEquals(0, Utils.changeOfBasis(new double[]{1, 1, 45}, new double[]{2, 2, 45})[2], 1e-6);
        assertEquals(1.0, Utils.changeOfBasis(new double[]{1, 1, 90}, new double[]{2, 2, 90})[0], 1e-6);
        assertEquals(-1.0, Utils.changeOfBasis(new double[]{1, 1, 90}, new double[]{2, 2, 90})[1], 1e-6);
        assertEquals(0, Utils.changeOfBasis(new double[]{1, 1, 90}, new double[]{2, 2, 90})[2], 1e-6);
    }

    @Test
    public void test_rad2deg() {
        assertEquals(180, Utils.rad2deg(Math.PI), 1e-6);
        assertEquals(90, Utils.rad2deg(Math.PI / 2), 1e-6);
        assertEquals(45, Utils.rad2deg(Math.PI / 4), 1e-6);
        assertEquals(0, Utils.rad2deg(0), 1e-6);
        assertEquals(-180, Utils.rad2deg(-Math.PI), 1e-6);
        assertEquals(-90, Utils.rad2deg(-Math.PI / 2), 1e-6);
    }

    @Test
    public void test_deg2rad() {
        assertEquals(Math.PI, Utils.deg2rad(180), 1e-6);
        assertEquals(Math.PI / 2, Utils.deg2rad(90), 1e-6);
        assertEquals(Math.PI / 4, Utils.deg2rad(45), 1e-6);
        assertEquals(0, Utils.deg2rad(0), 1e-6);
        assertEquals(-Math.PI, Utils.deg2rad(-180), 1e-6);
        assertEquals(-Math.PI / 2, Utils.deg2rad(-90), 1e-6);
    }

    @Test
    public void test_sign() {
        assertEquals(1, Utils.sign(1));
        assertEquals(-1, Utils.sign(-1));
        assertEquals(1, Utils.sign(0));
        assertEquals(1, Utils.sign(100));
        assertEquals(-1, Utils.sign(-100));
        assertEquals(1, Utils.sign(0.5));
        assertEquals(-1, Utils.sign(-0.5));
    }
}
