public class DefaultMapping {

    public int buttonId;
    public int buttonType;
    public int value;
    public boolean connectingUsingDs4drv;
    public int[] overflow;

    public DefaultMapping(int buttonId, int buttonType, int value, boolean connectingUsingDs4drv) {
        this(buttonId, buttonType, value, connectingUsingDs4drv, null, false);
    }

    public DefaultMapping(int buttonId, int buttonType, int value, boolean connectingUsingDs4drv, int[] overflow, boolean debug) {
        this.buttonId = buttonId;
        this.buttonType = buttonType;
        this.value = value;
        this.connectingUsingDs4drv = connectingUsingDs4drv;
        this.overflow = overflow;
        if (debug) {
            System.out.printf("button_id: %d button_type: %d value: %d overflow: %s%n", this.buttonId, this.buttonType, this.value, this.overflow);
        }
    }

    // Setter methods
    public void setButtonType(int buttonType) {
        this.buttonType = buttonType;
    }

    public void setButtonId(int buttonId) {
        this.buttonId = buttonId;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setConnectingUsingDs4drv(boolean connectingUsingDs4drv) {
        this.connectingUsingDs4drv = connectingUsingDs4drv;
    }

    // L joystick group #
    public boolean L3_event() {
        return this.buttonType == 2 && (this.buttonId == 1 || this.buttonId == 0);
    }

    public boolean L3_y_at_rest() {
        return this.buttonId == 1 && this.value == 0;
    }

    public boolean L3_x_at_rest() {
        return this.buttonId == 0 && this.value == 0;
    }

    public boolean L3_up() {
        return this.buttonId == 1 && this.value < 0;
    }

    public boolean L3_down() {
        return this.buttonId == 1 && this.value > 0;
    }

    public boolean L3_left() {
        return this.buttonId == 0 && this.value < 0;
    }

    public boolean L3_right() {
        return this.buttonId == 0 && this.value > 0;
    }

    public boolean L3_pressed() {
        if (!this.connectingUsingDs4drv) {
            return this.buttonId == 11 && this.buttonType == 1 && this.value == 1;
        }
        return false;
    }

    public boolean L3_released() {
        if (!this.connectingUsingDs4drv) {
            return this.buttonId == 11 && this.buttonType == 1 && this.value == 0;
        }
        return false;
    }

    // R joystick group #
    public boolean R3_event() {
        if (!this.connectingUsingDs4drv) {
            return this.buttonType == 2 && (this.buttonId == 4 || this.buttonId == 3);
        }
        return this.buttonType == 2 && (this.buttonId == 5 || this.buttonId == 2);
    }

    public boolean R3_y_at_rest() {
        if (!this.connectingUsingDs4drv) {
            return this.buttonId == 4 && this.value == 0;
        }
        return this.buttonId == 2 && this.value == 0;
    }

    public boolean R3_x_at_rest() {
        if (!this.connectingUsingDs4drv) {
            return this.buttonId == 3 && this.value == 0;
        }
        return this.buttonId == 5 && this.value == 0;
    }

    public boolean R3_up() {
        if (!this.connectingUsingDs4drv) {
            return this.buttonId == 4 && this.value < 0;
        }
        return this.buttonId == 5 && this.value < 0;
    }

    public boolean R3_down() {
        if (!this.connectingUsingDs4drv) {
            return this.buttonId == 4 && this.value > 0;
        }
        return this.buttonId == 5 && this.value > 0;
    }

    public boolean R3_left() {
        if (!this.connectingUsingDs4drv) {
            return this.buttonId == 3 && this.value < 0;
        }
        return this.buttonId == 2 && this.value < 0;
    }

    public boolean R3_right() {
        if (!this.connectingUsingDs4drv) {
            return this.buttonId == 3 && this.value > 0;
        }
        return this.buttonId == 2 && this.value > 0;
    }

    public boolean R3_pressed() {
        if (!this.connectingUsingDs4drv) {
            return this.buttonId == 12 && this.buttonType == 1 && this.value == 1;
        }
        return false;
    }

    public boolean R3_released() {
        if (!this.connectingUsingDs4drv) {
            return this.buttonId == 12 && this.buttonType == 1 && this.value == 0;
        }
        return false;
    }

    // Square / Triangle / Circle / X Button group #
    public boolean circle_pressed() {
        return this.buttonId == 2 && this.buttonType == 1 && this.value == 1;
    }

    public boolean circle_released() {
        return this.buttonId == 2 && this.buttonType == 1 && this.value == 0;
    }

    public boolean x_pressed() {
        return this.buttonId == 1 && this.buttonType == 1 && this.value == 1;
    }

    public boolean x_released() {
        return this.buttonId == 1 && this.buttonType == 1 && this.value == 0;
    }

    public boolean triangle_pressed() {
        return this.buttonId == 3 && this.buttonType == 1 && this.value == 1;
    }

    public boolean triangle_released() {
        return this.buttonId == 3 && this.buttonType == 1 && this.value == 0;
    }

    public boolean square_pressed() {
        return this.buttonId == 0 && this.buttonType == 1 && this.value == 1;
    }

    public boolean square_released() {
        return this.buttonId == 0 && this.buttonType == 1 && this.value == 0;
    }

    public boolean options_pressed() {
        return this.buttonId == 9 && this.buttonType == 1 && this.value == 1;
    }

    public boolean options_released() {
        return this.buttonId == 9 && this.buttonType == 1 && this.value == 0;
    }

    public boolean share_pressed() {
        if (!this.connectingUsingDs4drv) {
            return this.buttonId == 8 && this.buttonType == 1 && this.value == 1;
        }
        return false;
    }

    public boolean share_released() {
        if (!this.connectingUsingDs4drv) {
            return this.buttonId == 8 && this.buttonType == 1 && this.value == 0;
        }
        return false;
    }

    // N1 group #
    public boolean L1_pressed() {
        return this.buttonId == 4 && this.buttonType == 1 && this.value == 1;
    }

    public boolean L1_released() {
        return this.buttonId == 4 && this.buttonType == 1 && this.value == 0;
    }

    public boolean R1_pressed() {
        return this.buttonId == 5 && this.buttonType == 1 && this.value == 1;
    }

    public boolean R1_released() {
        return this.buttonId == 5 && this.buttonType == 1 && this.value == 0;
    }

    // N2 group #
    public boolean L2_pressed() {
        if (!this.connectingUsingDs4drv) {
            return this.buttonId == 2 && this.buttonType == 2 && (32767 >= this.value && this.value >= -32766);
        }
        return this.buttonId == 3 && this.buttonType == 2 && (32767 >= this.value && this.value >= -32766);
    }

    public boolean L2_released() {
        if (!this.connectingUsingDs4drv) {
            return this.buttonId == 2 && this.buttonType == 2 && this.value == -32767;
        }
        return this.buttonId == 3 && this.buttonType == 2 && this.value == -32767;
    }

    public boolean R2_pressed() {
        if (!this.connectingUsingDs4drv) {
            return this.buttonId == 5 && this.buttonType == 2 && (32767 >= this.value && this.value >= -32766);
        }
        return this.buttonId == 4 && this.buttonType == 2 && (32767 >= this.value && this.value >= -32766);
    }

    public boolean R2_released() {
        if (!this.connectingUsingDs4drv) {
            return this.buttonId == 5 && this.buttonType == 2 && this.value == -32767;
        }
        return this.buttonId == 4 && this.buttonType == 2 && this.value == -32767;
    }

    // up / down arrows #
    public boolean up_arrow_pressed() {
        if (!this.connectingUsingDs4drv) {
            return this.buttonId == 7 && this.buttonType == 2 && this.value == -32767;
        }
        return this.buttonId == 10 && this.buttonType == 2 && this.value == -32767;
    }

    public boolean down_arrow_pressed() {
        if (!this.connectingUsingDs4drv) {
            return this.buttonId == 7 && this.buttonType == 2 && this.value == 32767;
        }
        return this.buttonId == 10 && this.buttonType == 2 && this.value == 32767;
    }

    public boolean up_down_arrow_released() {
        if (!this.connectingUsingDs4drv) {
            return this.buttonId == 7 && this.buttonType == 2 && this.value == 0;
        }
        return this.buttonId == 10 && this.buttonType == 2 && this.value == 0;
    }

    // left / right arrows #
    public boolean left_arrow_pressed() {
        if (!this.connectingUsingDs4drv) {
            return this.buttonId == 6 && this.buttonType == 2 && this.value == -32767;
        }
        return this.buttonId == 9 && this.buttonType == 2 && this.value == -32767;
    }

    public boolean right_arrow_pressed() {
        if (!this.connectingUsingDs4drv) {
            return this.buttonId == 6 && this.buttonType == 2 && this.value == 32767;
        }
        return this.buttonId == 9 && this.buttonType == 2 && this.value == 32767;
    }

    public boolean left_right_arrow_released() {
        if (!this.connectingUsingDs4drv) {
            return this.buttonId == 6 && this.buttonType == 2 && this.value == 0;
        }
        return this.buttonId == 9 && this.buttonType == 2 && this.value == 0;
    }

    public boolean playstation_button_pressed() {
        if (!this.connectingUsingDs4drv) {
            return this.buttonId == 10 && this.buttonType == 1 && this.value == 1;
        }
        return false;
    }

    public boolean playstation_button_released() {
        if (!this.connectingUsingDs4drv) {
            return this.buttonId == 10 && this.buttonType == 1 && this.value == 0;
        }
        return false;
    }
}
