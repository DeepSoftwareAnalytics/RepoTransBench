public class LabelDuration {
    private String label;
    private double duration;

    public LabelDuration(String label, double duration) {
        this.label = label;
        this.duration = duration;
    }

    public String getLabel() {
        return label;
    }

    public double getDuration() {
        return duration;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LabelDuration that = (LabelDuration) obj;
        return Double.compare(that.duration, duration) == 0 && label.equals(that.label);
    }
}
