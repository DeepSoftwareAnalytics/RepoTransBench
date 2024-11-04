import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
public class TestColorize {

    private Colorize c;

    @Before
    public void setUp() {
        c = new Colorize();
    }

    @Test
    public void testGrey() {
        assertEquals("\u001b[30;1mColorize\u001b[0m", c.grey("Colorize"));
    }

    @Test
    public void testRed() {
        assertEquals("\u001b[31mColorize\u001b[0m", c.red("Colorize"));
    }

    @Test
    public void testGreen() {
        assertEquals("\u001b[32mColorize\u001b[0m", c.green("Colorize"));
    }

    @Test
    public void testYellow() {
        assertEquals("\u001b[33mColorize\u001b[0m", c.yellow("Colorize"));
    }

    @Test
    public void testBlue() {
        assertEquals("\u001b[34mColorize\u001b[0m", c.blue("Colorize"));
    }

    @Test
    public void testPink() {
        assertEquals("\u001b[35mColorize\u001b[0m", c.pink("Colorize"));
    }

    @Test
    public void testLightBlue() {
        assertEquals("\u001b[36mColorize\u001b[0m", c.light_blue("Colorize"));
    }
}
