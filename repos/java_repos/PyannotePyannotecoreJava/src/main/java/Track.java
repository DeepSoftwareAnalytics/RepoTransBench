public class Track {
    private Segment segment;
    private String trackName;

    public Track(Segment segment, String trackName) {
        this.segment = segment;
        this.trackName = trackName;
    }

    public Segment getSegment() {
        return segment;
    }

    public String getTrackName() {
        return trackName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Track track = (Track) obj;
        return segment.equals(track.segment) && trackName.equals(track.trackName);
    }
}
