import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import draw.DrawUtils;

public class TestDraw {

    @Test
    public void test_scale() {
        assertEquals(40, DrawUtils.scale(1));
        assertArrayEquals(new int[]{40, 80}, DrawUtils.scale(new int[]{1, 2}));
        assertArrayEquals(new int[]{40, 80}, DrawUtils.scale(new int[]{1, 2}));
        assertEquals(0, DrawUtils.scale(0));
        assertEquals(-40, DrawUtils.scale(-1));
    }

    @Test
    public void test_unscale() {
        assertEquals(1, DrawUtils.unscale(40));
        assertArrayEquals(new int[]{1, 2}, DrawUtils.unscale(new int[]{40, 80}));
        assertArrayEquals(new int[]{1, 2}, DrawUtils.unscale(new int[]{40, 80}));
        assertEquals(0, DrawUtils.unscale(0));
        assertEquals(-1, DrawUtils.unscale(-40));
    }
}
