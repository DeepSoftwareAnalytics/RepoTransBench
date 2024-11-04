public class TrackWithLabel extends Track {
    private String label;

    public TrackWithLabel(Segment segment, String trackName, String label) {
        super(segment, trackName);
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        TrackWithLabel that = (TrackWithLabel) obj;
        return label.equals(that.label);
    }
}
