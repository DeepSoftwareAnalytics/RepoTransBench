import org.jline.reader.Candidate;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CommandTest {

    @Test
    void testCommandCreation() {
        Command cmd = new Command("configure");
        assertEquals("configure", cmd.getName());
        assertEquals("No help provided", cmd.getHelp());
        assertFalse(cmd.isDynamicArgs());
    }

    @Test
    void testSimpleCompletion() {
        Command cmd1 = new Command("configure");
        Command cmd2 = new Command("terminal");
        cmd1.addChild(cmd2);
        List<Candidate> candidates = new ArrayList<>();
        cmd1.complete(new String[]{""}, candidates, "configure ");
        assertEquals("terminal ", candidates.get(0).value());
    }

    @Test
    void testDoubleCompletion() {
        Command cmd1 = new Command("configure");
        Command cmd2 = new Command("terminal");
        Command cmd3 = new Command("interface");
        cmd1.addChild(cmd2);
        cmd1.addChild(cmd3);
        List<Candidate> candidates = new ArrayList<>();
        cmd1.complete(new String[]{""}, candidates, "configure ");
        assertEquals(2, candidates.size());
        assertNotNull(candidates.stream().filter(c -> c.value().equals("interface ")).findFirst().orElse(null));
        assertNotNull(candidates.stream().filter(c -> c.value().equals("terminal ")).findFirst().orElse(null));
    }

    @Test
    void testCompletionWithBuffer() {
        Command cmd1 = new Command("configure");
        Command cmd2 = new Command("terminal");
        cmd1.addChild(cmd2);
        List<Candidate> candidates = new ArrayList<>();
        cmd1.complete(new String[]{"t"}, candidates, "configure t");
        assertEquals("terminal ", candidates.get(0).value());
    }

    @Test
    void testCompletionWithDynamicArg() {
        Command cmd1 = new Command("show");
        Command cmd2 = new Command("call", true);
        Command cmd3 = new Command("calls", true);
        cmd2.setArgs(() -> Arrays.asList("100", "101"));
        cmd3.setArgs(() -> Arrays.asList("continuous", "raw"));
        cmd1.addChild(cmd2);
        cmd1.addChild(cmd3);

        List<Candidate> candidates = new ArrayList<>();
        cmd1.complete(new String[]{"c"}, candidates, "show calls");
        assertNotNull(candidates.stream().filter(c -> c.value().equals("calls ")).findFirst().orElse(null));
        assertNotNull(candidates.stream().filter(c -> c.value().equals("call ")).findFirst().orElse(null));
    }
}
