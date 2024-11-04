import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class TestCoverageBadge {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    @Before
    public void setUpMocks() {
        // Mock get_total method to always return "79"
        CoverageBadgeMain mockedMain = mock(CoverageBadgeMain.class);
        when(mockedMain.get_total()).thenReturn("79");
    }

    @Test
    public void testVersionOutput() {
        String[] args = {"-v"};
        try {
            CoverageBadgeMain.main(args);
            fail("Expected SystemExit not thrown");
        } catch (SecurityException e) {
            assertEquals("coverage-badge v" + CoverageBadgeMain.VERSION + "\n", outContent.toString());
        }
    }

    @Test
    public void testSvgOutput() {
        String[] args = {};
        CoverageBadgeMain.main(args);
        String output = outContent.toString();
        assertTrue(output.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(output.contains("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"99\" height=\"20\">"));
        assertTrue(output.contains("<text x=\"80\" y=\"14\">79%</text>"));
        assertTrue(output.endsWith("</svg>\n"));
    }

    @Test
    public void testColorRanges() {
        String[] totals = {"97", "93", "80", "65", "45", "15", "n/a"};
        String[] expectedColors = {"#4c1", "#97CA00", "#a4a61d", "#dfb317", "#fe7d37", "#e05d44", "#9f9f9f"};

        for (int i = 0; i < totals.length; i++) {
            CoverageBadgeMain mockedMain = mock(CoverageBadgeMain.class);
            when(mockedMain.get_total()).thenReturn(totals[i]);

            String[] args = {};
            CoverageBadgeMain.main(args);
            String output = outContent.toString();
            String row = "<path fill=\"" + expectedColors[i] + "\" d=\"M63 0h36v20H63z\"/>";
            assertTrue(output.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
            assertTrue(output.contains(row));
            assertTrue(output.endsWith("</svg>\n"));
            outContent.reset();
        }
    }

    @Test
    public void testPlainColorMode() {
        String[] totals = {"97", "93", "80", "65", "45", "15", "n/a"};
        String defaultColor = "#a4a61d";

        assertEquals(CoverageBadgeMain.DEFAULT_COLOR, defaultColor);

        for (String total : totals) {
            CoverageBadgeMain mockedMain = mock(CoverageBadgeMain.class);
            when(mockedMain.get_total()).thenReturn(total);

            String[] args = {"-p"};
            CoverageBadgeMain.main(args);
            String output = outContent.toString();
            String row = "<path fill=\"" + defaultColor + "\" d=\"M63 0h36v20H63z\"/>";
            assertTrue(output.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
            assertTrue(output.contains(row));
            assertTrue(output.endsWith("</svg>\n"));
            outContent.reset();
        }
    }
}
