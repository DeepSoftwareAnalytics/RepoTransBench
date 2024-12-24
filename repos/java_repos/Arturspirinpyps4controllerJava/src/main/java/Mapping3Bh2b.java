public class Mapping3Bh2b extends DefaultMapping {

    public Mapping3Bh2b(int buttonId, int buttonType, int value, boolean connectingUsingDs4drv, int[] overflow) {
        this(buttonId, buttonType, value, connectingUsingDs4drv, overflow, false);
    }

    public Mapping3Bh2b(int buttonId, int buttonType, int value, boolean connectingUsingDs4drv, int[] overflow, boolean debug) {
        super(overflow[2], overflow[1], overflow[0], connectingUsingDs4drv, overflow, debug);
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

    // Square / Triangle / Circle / X Button group #
    public boolean circle_pressed() {
        return this.buttonId == 1 && this.buttonType == 1 && this.value == 1;
    }

    public boolean circle_released() {
        return this.buttonId == 1 && this.buttonType == 1 && this.value == 0;
    }

    public boolean x_pressed() {
        return this.buttonId == 0 && this.buttonType == 1 && this.value == 1;
    }

    public boolean x_released() {
        return this.buttonId == 0 && this.buttonType == 1 && this.value == 0;
    }

    public boolean triangle_pressed() {
        return this.buttonId == 2 && this.buttonType == 1 && this.value == 1;
    }

    public boolean triangle_released() {
        return this.buttonId == 2 && this.buttonType == 1 && this.value == 0;
    }

    public boolean square_pressed() {
        return this.buttonId == 3 && this.buttonType == 1 && this.value == 1;
    }

    public boolean square_released() {
        return this.buttonId == 3 && this.buttonType == 1 && this.value == 0;
    }
}
