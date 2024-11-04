import java.util.ArrayList;
import java.util.List;

public class Timeline {
    private String name;
    private List<Segment> segments;

    public Timeline(String name) {
        this.name = name;
        this.segments = new ArrayList<>();
    }

    public void addSegment(Segment segment) {
        segments.add(segment);
    }

    public Annotation toAnnotation(String modality) {
        Annotation annotation = new Annotation(name, modality);
        for (Segment segment : segments) {
            annotation.addSegment(segment, "D", "Label");
        }
        return annotation;
    }

    public Segment extent() {
        double minStart = Double.MAX_VALUE;
        double maxEnd = Double.MIN_VALUE;
        for (Segment segment : segments) {
            if (segment.getStart() < minStart) {
                minStart = segment.getStart();
            }
            if (segment.getEnd() > maxEnd) {
                maxEnd = segment.getEnd();
            }
        }
        return new Segment(minStart, maxEnd);
    }

    public List<Segment> support() {
        return support(0);
    }

    public List<Segment> support(double threshold) {
        List<Segment> supportSegments = new ArrayList<>();
        Segment current = null;
        for (Segment segment : segments) {
            if (current == null) {
                current = segment;
            } else if (current.getEnd() >= segment.getStart() - threshold) {
                current = new Segment(current.getStart(), Math.max(current.getEnd(), segment.getEnd()));
            } else {
                supportSegments.add(current);
                current = segment;
            }
        }
        if (current != null) {
            supportSegments.add(current);
        }
        return supportSegments;
    }

    public List<Segment> gaps() {
        List<Segment> gaps = new ArrayList<>();
        for (int i = 1; i < segments.size(); i++) {
            Segment prev = segments.get(i - 1);
            Segment current = segments.get(i);
            if (current.getStart() > prev.getEnd()) {
                gaps.add(new Segment(prev.getEnd(), current.getStart()));
            }
        }
        return gaps;
    }
}
