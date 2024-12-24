public class Scale {
    public final double scale;
    public final String unit;
    public final String name;

    public Scale(String unit) {
        switch (unit) {
            case "s":
                this.scale = 1;
                this.unit = "s";
                this.name = "Seconds";
                break;
            case "ms":
                this.scale = 1e3;
                this.unit = "ms";
                this.name = "Milliseconds";
                break;
            case "us":
                this.scale = 1e6;
                this.unit = "us";
                this.name = "Microseconds";
                break;
            case "ns":
            default:
                this.scale = 1e9;
                this.unit = "ns";
                this.name = "Nanoseconds";
                break;
        }
    }
}
