package reeds_shepp;

public enum Steering {
    LEFT(-1), RIGHT(1), STRAIGHT(0);

    private final int value;

    Steering(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
