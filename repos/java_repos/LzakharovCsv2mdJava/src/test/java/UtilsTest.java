import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilsTest {
    @Test
    public void testColumnLetter() {
        assertEquals("a", Utils.columnLetter(0));
        assertEquals("e", Utils.columnLetter(4));
        assertEquals("ee", Utils.columnLetter(30));
    }
}
