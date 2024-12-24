import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.Arrays;
public class TestHelpers {

    @Test
    public void testBase36() {
        Map<Integer, String> knownMappings = new HashMap<>();
        knownMappings.put(96192, "2280");
        knownMappings.put(53248, "1534");
        knownMappings.put(84896, "1ti8");
        knownMappings.put(28355, "lvn");
        knownMappings.put(57908, "18ok");
        knownMappings.put(52478, "14hq");
        knownMappings.put(88436, "1w8k");
        knownMappings.put(93482, "204q");
        knownMappings.put(19069, "epp");
        knownMappings.put(97614, "23bi");
        knownMappings.put(0, "0");

        for (Map.Entry<Integer, String> entry : knownMappings.entrySet()) {
            assertEquals(entry.getValue(), Cuid.toBase36(entry.getKey()));
        }
    }

    @Test
    public void testPad() {
        assertEquals("001234", Cuid.pad("1234", 6));
        assertEquals("234", Cuid.pad("1234", 3));
    }

    @Test
    public void testRandomBlock() {
        String[] blocks = new String[10];
        for (int i = 0; i < 10; i++) {
            blocks[i] = Cuid.randomBlock();
        }
        for (String b : blocks) {
            assertEquals(Cuid.BLOCK_SIZE, b.length());
        }
        
        Set<String> uniqueBlocks = new HashSet<>(Arrays.asList(blocks));
        assertEquals(uniqueBlocks.size(), blocks.length);
    }
}
