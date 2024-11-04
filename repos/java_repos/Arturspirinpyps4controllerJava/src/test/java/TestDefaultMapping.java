import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestDefaultMapping {
    private DefaultMapping mapping;

    @Before
    public void setUp() {
        mapping = new DefaultMapping(0, 0, 0, false);
    }

    @Test
    public void testL3Event() {
        mapping.setButtonType(2);
        mapping.setButtonId(0);
        assertTrue(mapping.L3_event());

        mapping.setButtonId(1);
        assertTrue(mapping.L3_event());

        mapping.setButtonId(2);
        assertFalse(mapping.L3_event());
    }

    @Test
    public void testL3YAtRest() {
        mapping.setButtonId(1);
        mapping.setValue(0);
        assertTrue(mapping.L3_y_at_rest());

        mapping.setValue(1);
        assertFalse(mapping.L3_y_at_rest());
    }

    @Test
    public void testL3XAtRest() {
        mapping.setButtonId(0);
        mapping.setValue(0);
        assertTrue(mapping.L3_x_at_rest());

        mapping.setValue(1);
        assertFalse(mapping.L3_x_at_rest());
    }

    @Test
    public void testL3Up() {
        mapping.setButtonId(1);
        mapping.setValue(-1);
        assertTrue(mapping.L3_up());

        mapping.setValue(1);
        assertFalse(mapping.L3_up());
    }

    @Test
    public void testL3Down() {
        mapping.setButtonId(1);
        mapping.setValue(1);
        assertTrue(mapping.L3_down());

        mapping.setValue(-1);
        assertFalse(mapping.L3_down());
    }

    @Test
    public void testL3Left() {
        mapping.setButtonId(0);
        mapping.setValue(-1);
        assertTrue(mapping.L3_left());

        mapping.setValue(1);
        assertFalse(mapping.L3_left());
    }

    @Test
    public void testL3Right() {
        mapping.setButtonId(0);
        mapping.setValue(1);
        assertTrue(mapping.L3_right());

        mapping.setValue(-1);
        assertFalse(mapping.L3_right());
    }

    @Test
    public void testL3Pressed() {
        mapping.setButtonId(11);
        mapping.setButtonType(1);
        mapping.setValue(1);
        mapping.setConnectingUsingDs4drv(false);
        assertTrue(mapping.L3_pressed());

        mapping.setConnectingUsingDs4drv(true);
        assertFalse(mapping.L3_pressed());
    }

    @Test
    public void testL3Released() {
        mapping.setButtonId(11);
        mapping.setButtonType(1);
        mapping.setValue(0);
        mapping.setConnectingUsingDs4drv(false);
        assertTrue(mapping.L3_released());

        mapping.setConnectingUsingDs4drv(true);
        assertFalse(mapping.L3_released());
    }

    @Test
    public void testR3Event() {
        mapping.setButtonType(2);
        mapping.setConnectingUsingDs4drv(false);
        mapping.setButtonId(3);
        assertTrue(mapping.R3_event());

        mapping.setButtonId(4);
        assertTrue(mapping.R3_event());

        mapping.setButtonId(5);
        assertFalse(mapping.R3_event());
    }

    @Test
    public void testR3YAtRest() {
        mapping.setButtonId(4);
        mapping.setValue(0);
        mapping.setConnectingUsingDs4drv(false);
        assertTrue(mapping.R3_y_at_rest());

        mapping.setValue(1);
        assertFalse(mapping.R3_y_at_rest());
    }

    @Test
    public void testR3XAtRest() {
        mapping.setButtonId(3);
        mapping.setValue(0);
        mapping.setConnectingUsingDs4drv(false);
        assertTrue(mapping.R3_x_at_rest());

        mapping.setValue(1);
        assertFalse(mapping.R3_x_at_rest());
    }

    @Test
    public void testR3Up() {
        mapping.setButtonId(4);
        mapping.setValue(-1);
        mapping.setConnectingUsingDs4drv(false);
        assertTrue(mapping.R3_up());

        mapping.setValue(1);
        assertFalse(mapping.R3_up());
    }

    @Test
    public void testR3Down() {
        mapping.setButtonId(4);
        mapping.setValue(1);
        mapping.setConnectingUsingDs4drv(false);
        assertTrue(mapping.R3_down());

        mapping.setValue(-1);
        assertFalse(mapping.R3_down());
    }

    @Test
    public void testR3Left() {
        mapping.setButtonId(3);
        mapping.setValue(-1);
        mapping.setConnectingUsingDs4drv(false);
        assertTrue(mapping.R3_left());

        mapping.setValue(1);
        assertFalse(mapping.R3_left());
    }

    @Test
    public void testR3Right() {
        mapping.setButtonId(3);
        mapping.setValue(1);
        mapping.setConnectingUsingDs4drv(false);
        assertTrue(mapping.R3_right());

        mapping.setValue(-1);
        assertFalse(mapping.R3_right());
    }

    @Test
    public void testCirclePressed() {
        mapping.setButtonId(2);
        mapping.setButtonType(1);
        mapping.setValue(1);
        assertTrue(mapping.circle_pressed());

        mapping.setValue(0);
        assertFalse(mapping.circle_pressed());
    }

    @Test
    public void testCircleReleased() {
        mapping.setButtonId(2);
        mapping.setButtonType(1);
        mapping.setValue(0);
        assertTrue(mapping.circle_released());

        mapping.setValue(1);
        assertFalse(mapping.circle_released());
    }

    @Test
    public void testXPressed() {
        mapping.setButtonId(1);
        mapping.setButtonType(1);
        mapping.setValue(1);
        assertTrue(mapping.x_pressed());

        mapping.setValue(0);
        assertFalse(mapping.x_pressed());
    }

    @Test
    public void testXReleased() {
        mapping.setButtonId(1);
        mapping.setButtonType(1);
        mapping.setValue(0);
        assertTrue(mapping.x_released());

        mapping.setValue(1);
        assertFalse(mapping.x_released());
    }

    @Test
    public void testTrianglePressed() {
        mapping.setButtonId(3);
        mapping.setButtonType(1);
        mapping.setValue(1);
        assertTrue(mapping.triangle_pressed());

        mapping.setValue(0);
        assertFalse(mapping.triangle_pressed());
    }

    @Test
    public void testTriangleReleased() {
        mapping.setButtonId(3);
        mapping.setButtonType(1);
        mapping.setValue(0);
        assertTrue(mapping.triangle_released());

        mapping.setValue(1);
        assertFalse(mapping.triangle_released());
    }

    @Test
    public void testSquarePressed() {
        mapping.setButtonId(0);
        mapping.setButtonType(1);
        mapping.setValue(1);
        assertTrue(mapping.square_pressed());

        mapping.setValue(0);
        assertFalse(mapping.square_pressed());
    }

    @Test
    public void testSquareReleased() {
        mapping.setButtonId(0);
        mapping.setButtonType(1);
        mapping.setValue(0);
        assertTrue(mapping.square_released());

        mapping.setValue(1);
        assertFalse(mapping.square_released());
    }

    @Test
    public void testOptionsPressed() {
        mapping.setButtonId(9);
        mapping.setButtonType(1);
        mapping.setValue(1);
        assertTrue(mapping.options_pressed());

        mapping.setValue(0);
        assertFalse(mapping.options_pressed());
    }

    @Test
    public void testOptionsReleased() {
        mapping.setButtonId(9);
        mapping.setButtonType(1);
        mapping.setValue(0);
        assertTrue(mapping.options_released());

        mapping.setValue(1);
        assertFalse(mapping.options_released());
    }

    @Test
    public void testSharePressed() {
        mapping.setButtonId(8);
        mapping.setButtonType(1);
        mapping.setValue(1);
        mapping.setConnectingUsingDs4drv(false);
        assertTrue(mapping.share_pressed());

        mapping.setConnectingUsingDs4drv(true);
        assertFalse(mapping.share_pressed());
    }

    @Test
    public void testShareReleased() {
        mapping.setButtonId(8);
        mapping.setButtonType(1);
        mapping.setValue(0);
        mapping.setConnectingUsingDs4drv(false);
        assertTrue(mapping.share_released());

        mapping.setConnectingUsingDs4drv(true);
        assertFalse(mapping.share_released());
    }

    @Test
    public void testL1Pressed() {
        mapping.setButtonId(4);
        mapping.setButtonType(1);
        mapping.setValue(1);
        assertTrue(mapping.L1_pressed());

        mapping.setValue(0);
        assertFalse(mapping.L1_pressed());
    }

    @Test
    public void testL1Released() {
        mapping.setButtonId(4);
        mapping.setButtonType(1);
        mapping.setValue(0);
        assertTrue(mapping.L1_released());

        mapping.setValue(1);
        assertFalse(mapping.L1_released());
    }

    @Test
    public void testR1Pressed() {
        mapping.setButtonId(5);
        mapping.setButtonType(1);
        mapping.setValue(1);
        assertTrue(mapping.R1_pressed());

        mapping.setValue(0);
        assertFalse(mapping.R1_pressed());
    }

    @Test
    public void testR1Released() {
        mapping.setButtonId(5);
        mapping.setButtonType(1);
        mapping.setValue(0);
        assertTrue(mapping.R1_released());

        mapping.setValue(1);
        assertFalse(mapping.R1_released());
    }

    @Test
    public void testL2Pressed() {
        mapping.setButtonId(2);
        mapping.setButtonType(2);
        mapping.setValue(0);
        mapping.setConnectingUsingDs4drv(false);
        assertTrue(mapping.L2_pressed());

        mapping.setValue(-32767);
        assertFalse(mapping.L2_pressed());
    }

    @Test
    public void testL2Released() {
        mapping.setButtonId(2);
        mapping.setButtonType(2);
        mapping.setValue(-32767);
        mapping.setConnectingUsingDs4drv(false);
        assertTrue(mapping.L2_released());

        mapping.setValue(0);
        assertFalse(mapping.L2_released());
    }

    @Test
    public void testR2Pressed() {
        mapping.setButtonId(5);
        mapping.setButtonType(2);
        mapping.setValue(0);
        mapping.setConnectingUsingDs4drv(false);
        assertTrue(mapping.R2_pressed());

        mapping.setValue(-32767);
        assertFalse(mapping.R2_pressed());
    }

    @Test
    public void testR2Released() {
        mapping.setButtonId(5);
        mapping.setButtonType(2);
        mapping.setValue(-32767);
        mapping.setConnectingUsingDs4drv(false);
        assertTrue(mapping.R2_released());

        mapping.setValue(0);
        assertFalse(mapping.R2_released());
    }

    @Test
    public void testUpArrowPressed() {
        mapping.setButtonId(7);
        mapping.setButtonType(2);
        mapping.setValue(-32767);
        mapping.setConnectingUsingDs4drv(false);
        assertTrue(mapping.up_arrow_pressed());

        mapping.setValue(0);
        assertFalse(mapping.up_arrow_pressed());
    }

    @Test
    public void testDownArrowPressed() {
        mapping.setButtonId(7);
        mapping.setButtonType(2);
        mapping.setValue(32767);
        mapping.setConnectingUsingDs4drv(false);
        assertTrue(mapping.down_arrow_pressed());

        mapping.setValue(0);
        assertFalse(mapping.down_arrow_pressed());
    }

    @Test
    public void testUpDownArrowReleased() {
        mapping.setButtonId(7);
        mapping.setButtonType(2);
        mapping.setValue(0);
        mapping.setConnectingUsingDs4drv(false);
        assertTrue(mapping.up_down_arrow_released());

        mapping.setValue(32767);
        assertFalse(mapping.up_down_arrow_released());
    }

    @Test
    public void testLeftArrowPressed() {
        mapping.setButtonId(6);
        mapping.setButtonType(2);
        mapping.setValue(-32767);
        mapping.setConnectingUsingDs4drv(false);
        assertTrue(mapping.left_arrow_pressed());

        mapping.setValue(0);
        assertFalse(mapping.left_arrow_pressed());
    }

    @Test
    public void testRightArrowPressed() {
        mapping.setButtonId(6);
        mapping.setButtonType(2);
        mapping.setValue(32767);
        mapping.setConnectingUsingDs4drv(false);
        assertTrue(mapping.right_arrow_pressed());

        mapping.setValue(0);
        assertFalse(mapping.right_arrow_pressed());
    }

    @Test
    public void testLeftRightArrowReleased() {
        mapping.setButtonId(6);
        mapping.setButtonType(2);
        mapping.setValue(0);
        mapping.setConnectingUsingDs4drv(false);
        assertTrue(mapping.left_right_arrow_released());

        mapping.setValue(32767);
        assertFalse(mapping.left_right_arrow_released());
    }

    @Test
    public void testPlaystationButtonPressed() {
        mapping.setButtonId(10);
        mapping.setButtonType(1);
        mapping.setValue(1);
        mapping.setConnectingUsingDs4drv(false);
        assertTrue(mapping.playstation_button_pressed());

        mapping.setConnectingUsingDs4drv(true);
        assertFalse(mapping.playstation_button_pressed());
    }

    @Test
    public void testPlaystationButtonReleased() {
        mapping.setButtonId(10);
        mapping.setButtonType(1);
        mapping.setValue(0);
        mapping.setConnectingUsingDs4drv(false);
        assertTrue(mapping.playstation_button_released());

        mapping.setConnectingUsingDs4drv(true);
        assertFalse(mapping.playstation_button_released());
    }
}
