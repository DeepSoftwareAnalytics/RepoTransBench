import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConsoleTest {

    @Test
    void testConsoleCreation() {
        Console c = new Console();
        assertTrue(c instanceof Console);
    }

    @Test
    void testConsoleHasPrompt() {
        Console c = new Console();
        assertEquals("Prompt", c.getPrompt());
        assertEquals(">", c.getPromptDelim());
    }

    @Test
    void testConsoleHasEmptyWelcomeMessage() {
        Console c = new Console();
        assertNull(c.getWelcomeMessage());
    }

    @Test
    void testConsoleHasWelcomeMessage() {
        Console c = new Console("Prompt", ">", "welcome message");
        assertEquals("welcome message", c.getWelcomeMessage());
    }
}
