public class SlidingWindow {
    private double start;
    private double duration;
    private double step;

    public SlidingWindow(double start, double duration, double step) {
        this.start = start;
        this.duration = duration;
        this.step = step;
    }

    public double getStart() {
        return start;
    }

    public double getDuration() {
        return duration;
    }

    public double getStep() {
        return step;
    }
}
