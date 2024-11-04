public class Segment {
    private double start;
    private double end;

    public Segment(double start, double end) {
        this.start = start;
        this.end = end;
    }

    public double getStart(){
        return start;
    }

    public double getEnd() {
        return end;
    }

    public double getDuration() {
        return end - start;
    }

    public double getMiddle() {
        return (start + end) / 2;
    }

    public boolean intersects(Segment other) {
        return this.start < other.end && this.end > other.start;
    }

    public Segment intersection(Segment other) {
        if (!intersects(other)) {
            return null;
        }
        return new Segment(Math.max(this.start, other.start), Math.min(this.end, other.end));
    }

    public boolean includes(Segment other) {
        return this.start <= other.start && this.end >= other.end;
    }

    public boolean overlaps(Segment other) {
        return this.start < other.end && this.end > other.start;
    }

    @Override
    public String toString() {
        return String.format("[ %02d:%02d:%02d.%03d -->  %02d:%02d:%02d.%03d]",
                (int) start / 3600, ((int) start % 3600) / 60, (int) start % 60, (int) (start * 1000) % 1000,
                (int) end / 3600, ((int) end % 3600) / 60, (int) end % 60, (int) (end * 1000) % 1000);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Segment segment = (Segment) obj;
        return Double.compare(segment.start, start) == 0 && Double.compare(segment.end, end) == 0;
    }
}
