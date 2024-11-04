import java.util.ArrayList;
import java.util.List;

public class Annotation {
    private String name;
    private String speaker;
    private List<TrackWithLabel> tracks;

    public Annotation(String name, String speaker) {
        this.name = name;
        this.speaker = speaker;
        this.tracks = new ArrayList<>();
    }

    public void addSegment(Segment segment, String trackName, String label) {
        tracks.add(new TrackWithLabel(segment, trackName, label));
    }

    public List<Segment> itersegments() {
        List<Segment> segments = new ArrayList<>();
        for (TrackWithLabel track : tracks) {
            segments.add(track.getSegment());
        }
        return segments;
    }

    public List<Track> itertracks() {
        List<Track> trackList = new ArrayList<>();
        for (TrackWithLabel track : tracks) {
            trackList.add(new Track(track.getSegment(), track.getTrackName()));
        }
        return trackList;
    }

    public List<TrackWithLabel> itertracksWithLabels() {
        return tracks;
    }

    public Timeline getTimeline(boolean withTracks) {
        Timeline timeline = new Timeline(name);
        for (TrackWithLabel track : tracks) {
            timeline.addSegment(track.getSegment());
        }
        return timeline;
    }

    public List<String> labels() {
        List<String> labels = new ArrayList<>();
        for (TrackWithLabel track : tracks) {
            if (!labels.contains(track.getLabel())) {
                labels.add(track.getLabel());
            }
        }
        return labels;
    }

    public List<String> getLabels(Segment segment) {
        List<String> labels = new ArrayList<>();
        for (TrackWithLabel track : tracks) {
            if (track.getSegment().equals(segment)) {
                labels.add(track.getLabel());
            }
        }
        return labels;
    }

    public double labelDuration(String label) {
        double duration = 0.0;
        for (TrackWithLabel track : tracks) {
            if (track.getLabel().equals(label)) {
                duration += track.getSegment().getDuration();
            }
        }
        return duration;
    }

    public List<LabelDuration> chart() {
        List<LabelDuration> chart = new ArrayList<>();
        for (String label : labels()) {
            chart.add(new LabelDuration(label, labelDuration(label)));
        }
        return chart;
    }

    public String argmax() {
        String maxLabel = null;
        double maxDuration = 0.0;
        for (LabelDuration ld : chart()) {
            if (ld.getDuration() > maxDuration) {
                maxDuration = ld.getDuration();
                maxLabel = ld.getLabel();
            }
        }
        return maxLabel;
    }
}
