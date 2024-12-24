import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.List;

public class TestAnnotation {

    private Annotation annotation;

    @Before
    public void setUp() {
        annotation = new Annotation("TheBigBangTheory.Season01.Episode01", "speaker");
        annotation.addSegment(new Segment(3, 5), "_", "Penny");
        annotation.addSegment(new Segment(5.5, 7), "_", "Leonard");
        annotation.addSegment(new Segment(8, 10), "_", "Penny");
        annotation.addSegment(new Segment(8, 10), "anything", "Sheldon");
    }

    @Test
    public void testCrop() {
        Annotation expected = new Annotation("TheBigBangTheory.Season01.Episode01", "speaker");
        expected.addSegment(new Segment(5.5, 7), "_", "Leonard");
        expected.addSegment(new Segment(8, 9), "_", "Penny");
        expected.addSegment(new Segment(8, 9), "anything", "Sheldon");
        
        Annotation actual = annotation.crop(new Segment(5, 9));
        assertEquals(expected, actual);
    }

    @Test
    public void testCropLoose() {
        Annotation expected = new Annotation("TheBigBangTheory.Season01.Episode01", "speaker");
        expected.addSegment(new Segment(5.5, 7), "_", "Leonard");
        expected.addSegment(new Segment(8, 10), "_", "Penny");
        expected.addSegment(new Segment(8, 10), "anything", "Sheldon");

        Annotation actual = annotation.crop(new Segment(5, 9), "loose");
        assertEquals(expected, actual);
    }

    @Test
    public void testCropStrict() {
        Annotation expected = new Annotation("TheBigBangTheory.Season01.Episode01", "speaker");
        expected.addSegment(new Segment(5.5, 7), "_", "Leonard");

        Annotation actual = annotation.crop(new Segment(5, 9), "strict");
        assertEquals(expected, actual);
    }

    @Test
    public void testCopy() {
        Annotation copy = annotation.copy();
        assertEquals(copy, annotation);
    }

    @Test
    public void testCreation() {
        List<Segment> expectedSegments = Arrays.asList(
            new Segment(3, 5),
            new Segment(5.5, 7),
            new Segment(8, 10)
        );
        assertEquals(expectedSegments, annotation.itersegments());

        List<Track> expectedTracks = Arrays.asList(
            new Track(new Segment(3, 5), "_"),
            new Track(new Segment(5.5, 7), "_"),
            new Track(new Segment(8, 10), "_"),
            new Track(new Segment(8, 10), "anything")
        );
        assertEquals(expectedTracks, annotation.itertracks());

        List<TrackWithLabel> expectedTracksWithLabels = Arrays.asList(
            new TrackWithLabel(new Segment(3, 5), "_", "Penny"),
            new TrackWithLabel(new Segment(5.5, 7), "_", "Leonard"),
            new TrackWithLabel(new Segment(8, 10), "_", "Penny"),
            new TrackWithLabel(new Segment(8, 10), "anything", "Sheldon")
        );
        assertEquals(expectedTracksWithLabels, annotation.itertracksWithLabels());
    }

    @Test
    public void testSegments() {
        Timeline expectedTimeline = new Timeline("TheBigBangTheory.Season01.Episode01");
        expectedTimeline.addSegment(new Segment(3, 5));
        expectedTimeline.addSegment(new Segment(5.5, 7));
        expectedTimeline.addSegment(new Segment(8, 10));
        assertEquals(expectedTimeline, annotation.getTimeline(false));
    }

    @Test
    public void testTracks() {
        assertFalse(annotation.hasTrack(new Segment(8, 10), "---"));
        assertEquals(annotation.getTracks(new Segment(8, 10)), Set.of("_", "anything"));

        List<Track> renamedTracks = annotation.renameTracks().itertracks();
        List<Track> expectedRenamedTracks = Arrays.asList(
            new Track(new Segment(3, 5), "A"),
            new Track(new Segment(5.5, 7), "B"),
            new Track(new Segment(8, 10), "C"),
            new Track(new Segment(8, 10), "D")
        );
        assertEquals(expectedRenamedTracks, renamedTracks);
    }

    @Test
    public void testLabels() {
        List<String> expectedLabels = Arrays.asList("Leonard", "Penny", "Sheldon");
        assertEquals(expectedLabels, annotation.labels());

        List<String> expectedLabelsForSegment = Arrays.asList("Penny", "Sheldon");
        assertEquals(expectedLabelsForSegment, annotation.getLabels(new Segment(8, 10)));
    }

    @Test
    public void testAnalyze() {
        assertEquals(4, annotation.labelDuration("Penny"), 0.001);
        List<LabelDuration> expectedChart = Arrays.asList(
            new LabelDuration("Penny", 4),
            new LabelDuration("Sheldon", 2),
            new LabelDuration("Leonard", 1.5)
        );
        assertEquals(expectedChart, annotation.chart());
        assertEquals("Penny", annotation.argmax());
    }

    @Test
    public void testRenameLabels() {
        Annotation expected = new Annotation("TheBigBangTheory.Season01.Episode01", "speaker");
        expected.addSegment(new Segment(3, 5), "_", "Kaley Cuoco");
        expected.addSegment(new Segment(5.5, 7), "_", "Johnny Galecki");
        expected.addSegment(new Segment(8, 10), "_", "Kaley Cuoco");
        expected.addSegment(new Segment(8, 10), "anything", "Jim Parsons");

        Map<String, String> mapping = Map.of(
            "Penny", "Kaley Cuoco",
            "Sheldon", "Jim Parsons",
            "Leonard", "Johnny Galecki"
        );
        assertEquals(expected, annotation.renameLabels(mapping));
    }
}
