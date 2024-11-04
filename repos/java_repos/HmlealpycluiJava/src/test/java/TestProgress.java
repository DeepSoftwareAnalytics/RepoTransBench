import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TestProgress {

    private Progress progress;

    @Before
    public void setUp() {
        progress = new Progress(100);
    }

    @Test
    public void testPercentProgressBar() {
        progress.update(0.8);
        assertEquals(80, progress.getPercent());
    }

    @Test
    public void testBarLength() {
        progress.update(0.8);
        assertEquals(80, progress.getBarLength());
    }

    @Test
    public void testEmptyProgressBar() {
        assertEquals(0, progress.getPercent());
        assertEquals(0, progress.getBarLength());
    }
}
