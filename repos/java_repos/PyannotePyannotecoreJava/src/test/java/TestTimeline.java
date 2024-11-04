import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.Arrays;

public class TestTimeline {

    private Timeline timeline;

    @Before
    public void setUp() {
        timeline = new Timeline("MyAudioFile");
        timeline.add(new Segment(6, 8));
        timeline.add(new Segment(0.5, 3));
        timeline.add(new Segment(8.5, 10));
        timeline.add(new Segment(1, 4));
        timeline.add(new Segment(5, 7));
        timeline.add(new Segment(7, 8));
    }

    @Test
    public void testToAnnotation() {
        Annotation expected = new Annotation("MyAudioFile", "MyModality");
        expected.add(new Segment(6, 8), "D");
        expected.add(new Segment(0.5, 3), "A");
        expected.add(new Segment(8.5, 10), "F");
        expected.add(new Segment(1, 4), "B");
        expected.add(new Segment(5, 7), "C");
        expected.add(new Segment(7, 8), "E");
        assertEquals(expected, timeline.toAnnotation("MyModality"));
    }

    @Test
    public void testIteration() {
        List<Segment> expectedSegments = Arrays.asList(
                new Segment(0.5, 3),
                new Segment(1, 4),
                new Segment(5, 7),
                new Segment(6, 8),
                new Segment(7, 8),
                new Segment(8.5, 10)
        );
        assertEquals(expectedSegments, timeline.getSegments());
    }

    @Test
    public void testRemove() {
        timeline.remove(new Segment(1, 4));
        timeline.remove(new Segment(5, 7));
        List<Segment> expectedSegments = Arrays.asList(
                new Segment(0.5, 3),
                new Segment(6, 8),
                new Segment(7, 8),
                new Segment(8.5, 10)
        );
        assertEquals(expectedSegments, timeline.getSegments());
    }

    @Test
    public void testGetter() {
        assertEquals(6, timeline.size());
        assertEquals("[ 00:00:01.000 -->  00:00:04.000]", timeline.getSegment(1).toString());
    }

    @Test
    public void testGetterNegative() {
        assertEquals(new Segment(7, 8), timeline.getSegment(-2));
    }

    @Test
    public void testExtent() {
        assertEquals(new Segment(0.5, 10), timeline.extent());
    }

    @Test
    public void testRemoveAndExtent() {
        Timeline t = new Timeline("MyAudioFile");
        t.add(new Segment(6, 8));
        t.add(new Segment(7, 9));
        t.add(new Segment(6, 9));

        t.remove(new Segment(6, 9));
        assertEquals(new Segment(6, 9), t.extent());
    }

    @Test
    public void testSupport() {
        // No collar (default).
        List<Segment> expectedSupport = Arrays.asList(
                new Segment(0.5, 4),
                new Segment(5, 8),
                new Segment(8.5, 10)
        );
        assertEquals(expectedSupport, timeline.support());

        // Collar of 600 ms.
        List<Segment> expectedSupportWithCollar = Arrays.asList(
                new Segment(0.5, 4),
                new Segment(5, 10)
        );
        assertEquals(expectedSupportWithCollar, timeline.support(0.600));
    }

    @Test
    public void testGaps() {
        List<Segment> expectedGaps = Arrays.asList(
                new Segment(4, 5),
                new Segment(8, 8.5)
        );
        assertEquals(expectedGaps, timeline.gaps());
    }

    @Test
    public void testEmptyGaps() {
        Timeline emptyTimeline = new Timeline("MyEmptyGaps");
        assertTrue(emptyTimeline.gaps().isEmpty());
    }

    @Test
    public void testCrop() {
        Segment selection = new Segment(3, 7);

        Timeline expectedIntersection = new Timeline("MyAudioFile");
        expectedIntersection.add(new Segment(3, 4));
        expectedIntersection.add(new Segment(5, 7));
        expectedIntersection.add(new Segment(6, 7));
        assertEquals(expectedIntersection, timeline.crop(selection, "intersection"));

        Timeline expectedStrict = new Timeline("MyAudioFile");
        expectedStrict.add(new Segment(5, 7));
        assertEquals(expectedStrict, timeline.crop(selection, "strict"));

        Timeline expectedLoose = new Timeline("pouet");
        expectedLoose.add(new Segment(1, 4));
        expectedLoose.add(new Segment(5, 7));
        expectedLoose.add(new Segment(6, 8));
        assertEquals(expectedLoose, timeline.crop(selection, "loose"));
    }

    @Test
    public void testCropMapping() {
        Timeline timeline = new Timeline(Arrays.asList(
                new Segment(0, 2),
                new Segment(1, 2),
                new Segment(3, 4)
        ));
        Object[] croppedAndMapping = timeline.crop(new Segment(1, 2), true);

        Timeline expectedCropped = new Timeline(Arrays.asList(new Segment(1, 2)));
        assertEquals(expectedCropped, croppedAndMapping[0]);

        @SuppressWarnings("unchecked")
        Map<Segment, List<Segment>> expectedMapping = Map.of(
                new Segment(1, 2), Arrays.asList(new Segment(0, 2), new Segment(1, 2))
        );
        assertEquals(expectedMapping, croppedAndMapping[1]);
    }

    @Test
    public void testUnion() {
        Timeline firstTimeline = new Timeline(Arrays.asList(
                new Segment(0, 1),
                new Segment(2, 3),
                new Segment(4, 5)
        ));
        Timeline secondTimeline = new Timeline(Arrays.asList(
                new Segment(1.5, 4.5)
        ));

        Timeline expectedUnion = new Timeline(Arrays.asList(
                new Segment(0, 1),
                new Segment(1.5, 4.5),
                new Segment(2, 3),
                new Segment(4, 5)
        ));
        assertEquals(expectedUnion, firstTimeline.union(secondTimeline));

        Timeline expectedCrop = new Timeline(Arrays.asList(
                new Segment(2, 3),
                new Segment(4, 4.5)
        ));
        assertEquals(expectedCrop, secondTimeline.crop(firstTimeline));

        List<Object[]> expectedCoIter = Arrays.asList(
                new Object[]{new Segment(2, 3), new Segment(1.5, 4.5)},
                new Object[]{new Segment(4, 5), new Segment(1.5, 4.5)}
        );
        assertEquals(expectedCoIter, firstTimeline.coIter(secondTimeline));
    }

    @Test
    public void testUnionExtent() {
        Timeline firstTimeline = new Timeline(Arrays.asList(
                new Segment(0, 1),
                new Segment(2, 3),
                new Segment(4, 5)
        ));
        Timeline secondTimeline = new Timeline(Arrays.asList(
                new Segment(1.5, 6)
        ));

        Timeline unionTimeline = firstTimeline.union(secondTimeline);
        assertEquals(new Segment(0, 6), unionTimeline.extent());
    }

    @Test
    public void testUpdateExtent() {
        Timeline timeline = new Timeline(Arrays.asList(
                new Segment(0, 1),
                new Segment(2, 3),
                new Segment(4, 5)
        ));
        Timeline otherTimeline = new Timeline(Arrays.asList(
                new Segment(1.5, 6)
        ));
        timeline.update(otherTimeline);
        assertEquals(new Segment(0, 6), timeline.extent());
    }

    @Test
    public void testTimelineOverlaps() {
        Timeline overlappedTimeline = new Timeline("La menuiserie mec");
        overlappedTimeline.add(new Segment(0, 10));
        overlappedTimeline.add(new Segment(5, 10));
        overlappedTimeline.add(new Segment(15, 20));
        overlappedTimeline.add(new Segment(18, 23));

        Timeline expectedOverlap = new Timeline();
        expectedOverlap.add(new Segment(5, 10));
        expectedOverlap.add(new Segment(18, 20));

        assertEquals(expectedOverlap, overlappedTimeline.getOverlap());
    }

    @Test
    public void testExtrude() {
        Segment removed = new Segment(2, 5);

        Timeline timeline = new Timeline("KINGJU");
        timeline.add(new Segment(0, 3));
        timeline.add(new Segment(2, 5));
        timeline.add(new Segment(6, 7));

        Timeline expectedIntersection = new Timeline();
        expectedIntersection.add(new Segment(0, 2));
        expectedIntersection.add(new Segment(6, 7));
        assertEquals(expectedIntersection, timeline.extrude(removed, "intersection"));

        Timeline expectedStrict = new Timeline("MCSALO");
        expectedStrict.add(new Segment(0, 3));
        expectedStrict.add(new Segment(6, 7));
        assertEquals(expectedStrict, timeline.extrude(removed, "strict"));

        Timeline expectedLoose = new Timeline("CADILLAC");
        expectedLoose.add(new Segment(6, 7));
        assertEquals(expectedLoose, timeline.extrude(removed, "loose"));
    }

    @Test
    public void testInitializedWithEmptySegments() {
        Timeline firstTimeline = new Timeline(Arrays.asList(
                new Segment(1, 5),
                new Segment(6, 6),
                new Segment(7, 7),
                new Segment(8, 10)
        ));

        Timeline secondTimeline = new Timeline(Arrays.asList(
                new Segment(1, 5),
                new Segment(8, 10)
        ));

        assertEquals(firstTimeline, secondTimeline);
    }

    @Test
    public void testAddedEmptySegments() {
        Timeline firstTimeline = new Timeline();
        firstTimeline.add(new Segment(1, 5));
        firstTimeline.add(new Segment(6, 6));
        firstTimeline.add(new Segment(7, 7));
        firstTimeline.add(new Segment(8, 10));

        Timeline secondTimeline = new Timeline();
        secondTimeline.add(new Segment(1, 5));
        secondTimeline.add(new Segment(8, 10));

        assertEquals(firstTimeline, secondTimeline);
    }

    @Test
    public void testConsistentTimelinesWithEmptySegments() {
        Timeline firstTimeline = new Timeline(Arrays.asList(
                new Segment(1, 5),
                new Segment(6, 6),
                new Segment(7, 7),
                new Segment(8, 10)
        ));

        Timeline secondTimeline = new Timeline();
        secondTimeline.add(new Segment(1, 5));
        secondTimeline.add(new Segment(6, 6));
        secondTimeline.add(new Segment(7, 7));
        secondTimeline.add(new Segment(8, 10));

        assertEquals(firstTimeline, secondTimeline);
    }
}
