package reeds_shepp;

public enum Gear {
    FORWARD(1), BACKWARD(-1);

    private final int value;

    Gear(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

