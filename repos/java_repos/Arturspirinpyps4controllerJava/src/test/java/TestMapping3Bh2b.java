import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestMapping3Bh2b {
    private Mapping3Bh2b mapping;

    @Before
    public void setUp() {
        int[] overflow = {0, 1, 0}; // Default overflow values
        mapping = new Mapping3Bh2b(0, 0, 0, false, overflow);
    }

    @Test
    public void testCirclePressed() {
        mapping.setButtonId(1);
        mapping.setButtonType(1);
        mapping.setValue(1);
        assertTrue(mapping.circle_pressed());

        mapping.setValue(0);
        assertFalse(mapping.circle_pressed());
    }

    @Test
    public void testCircleReleased() {
        mapping.setButtonId(1);
        mapping.setButtonType(1);
        mapping.setValue(0);
        assertTrue(mapping.circle_released());

        mapping.setValue(1);
        assertFalse(mapping.circle_released());
    }

    @Test
    public void testXPressed() {
        mapping.setButtonId(0);
        mapping.setButtonType(1);
        mapping.setValue(1);
        assertTrue(mapping.x_pressed());

        mapping.setValue(0);
        assertFalse(mapping.x_pressed());
    }

    @Test
    public void testXReleased() {
        mapping.setButtonId(0);
        mapping.setButtonType(1);
        mapping.setValue(0);
        assertTrue(mapping.x_released());

        mapping.setValue(1);
        assertFalse(mapping.x_released());
    }

    @Test
    public void testTrianglePressed() {
        mapping.setButtonId(2);
        mapping.setButtonType(1);
        mapping.setValue(1);
        assertTrue(mapping.triangle_pressed());

        mapping.setValue(0);
        assertFalse(mapping.triangle_pressed());
    }

    @Test
    public void testTriangleReleased() {
        mapping.setButtonId(2);
        mapping.setButtonType(1);
        mapping.setValue(0);
        assertTrue(mapping.triangle_released());

        mapping.setValue(1);
        assertFalse(mapping.triangle_released());
    }

    @Test
    public void testSquarePressed() {
        mapping.setButtonId(3);
        mapping.setButtonType(1);
        mapping.setValue(1);
        assertTrue(mapping.square_pressed());

        mapping.setValue(0);
        assertFalse(mapping.square_pressed());
    }

    @Test
    public void testSquareReleased() {
        mapping.setButtonId(3);
        mapping.setButtonType(1);
        mapping.setValue(0);
        assertTrue(mapping.square_released());

        mapping.setValue(1);
        assertFalse(mapping.square_released());
    }
}
