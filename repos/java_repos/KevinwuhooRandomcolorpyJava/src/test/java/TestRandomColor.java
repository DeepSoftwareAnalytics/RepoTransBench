import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import java.util.List;
import java.util.Arrays;
import java.io.IOException;

public class TestRandomColor {

    private RandomColor randColor;
    private int pyVersion;

    @Before
    public void setUp() throws IOException {
        randColor = new RandomColor(42L);
        pyVersion = System.getProperty("java.version").startsWith("1.8") ? 2 : 3; // Assuming Java 8 is equivalent to Python 2
    }

    @Test
    public void testCount() {
        Assert.assertEquals(1, randColor.generate(null, null, 1, "hex").size());

        int numToGenerate = 10;
        List<String> colors = randColor.generate(null, null, numToGenerate, "hex");
        Assert.assertEquals(numToGenerate, colors.size());
    }

    @Test
    public void testHue() {
        List<String> expectedColors;
        if (pyVersion == 3) {
            expectedColors = Arrays.asList("#b98bd3", "#ac5ed1", "#a786d6");
        } else {
            expectedColors = Arrays.asList("#dec0f7", "#6d2cd6", "#d5abea");
        }

        List<String> purple = randColor.generate("purple", null, 3, "hex");
        Assert.assertEquals(expectedColors, purple);
    }

    @Test
    public void testLuminosity() {
        List<String> expectedColors;
        if (pyVersion == 3) {
            expectedColors = Arrays.asList("#d35098", "#3dce6e", "#dbf760");
        } else {
            expectedColors = Arrays.asList("#5061b7", "#95d319", "#ce56a2");
        }

        List<String> bright = randColor.generate(null, "bright", 3, "hex");
        Assert.assertEquals(expectedColors, bright);
    }

    @Test
    public void testHueLuminosity() {
        List<String> expectedColor;
        if (pyVersion == 3) {
            expectedColor = Arrays.asList("#b27910");
        } else {
            expectedColor = Arrays.asList("#bf7a13");
        }

        List<String> color = randColor.generate("orange", "dark", 1, "hex");
        Assert.assertEquals(expectedColor, color);
    }

    @Test
    public void testFormat() {
        List<String> expectedColorRgb;
        List<String> expectedColorHex;
        if (pyVersion == 3) {
            expectedColorRgb = Arrays.asList("rgb(7, 7, 7)");
            expectedColorHex = Arrays.asList("#4f4f4f");
        } else {
            expectedColorRgb = Arrays.asList("rgb(5, 5, 5)");
            expectedColorHex = Arrays.asList("#383838");
        }

        List<String> colorRgb = randColor.generate("monochrome", null, 1, "rgb");
        List<String> colorHex = randColor.generate("monochrome", null, 1, "hex");

        Assert.assertEquals(expectedColorRgb, colorRgb);
        Assert.assertEquals(expectedColorHex, colorHex);
    }

    @Test
    public void testSeed() throws IOException{
        List<String> expectedColor;
        if (pyVersion == 3) {
            expectedColor = Arrays.asList("#e094be");
        } else {
            expectedColor = Arrays.asList("#c0caf7");
        }

        List<String> color = randColor.generate(null, null, 1, "hex");
        Assert.assertEquals(expectedColor, color);
        Assert.assertEquals(expectedColor, new RandomColor(42L).generate(null, null, 1, "hex"));
    }
}
