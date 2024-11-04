package reeds_shepp;

public class PathElement {
    private final double param;
    private final Steering steering;
    private final Gear gear;

    private PathElement(double param, Steering steering, Gear gear) {
        this.param = param;
        this.steering = steering;
        this.gear = gear;
    }

    public static PathElement create(double param, Steering steering, Gear gear) {
        if (param >= 0) {
            return new PathElement(param, steering, gear);
        } else {
            return new PathElement(-param, steering, gear).reverseGear();
        }
    }

    public double getParam() {
        return param;
    }

    public Steering getSteering() {
        return steering;
    }

    public Gear getGear() {
        return gear;
    }

    @Override
    public String toString() {
        return "{ Steering: " + steering.name() + "\tGear: " + gear.name() + "\tdistance: " + Math.round(param) + " }";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        PathElement that = (PathElement) obj;

        if (Double.compare(that.param, param) != 0) return false;
        if (steering != that.steering) return false;
        return gear == that.gear;
    }

    public PathElement reverseSteering() {
        Steering reversedSteering = Steering.values()[-steering.getValue() + 1];
        return new PathElement(param, reversedSteering, gear);
    }

    public PathElement reverseGear() {
        Gear reversedGear = Gear.values()[-gear.getValue() + 1];
        return new PathElement(param, steering, reversedGear);
    }
}
