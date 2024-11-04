import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;

public class ProfViewTest {

    @BeforeEach
    public void setUp() {
        System.out.println("==============");
        System.out.println("prof_view test");
        System.out.println("==============");
    }

    @Test
    public void test_prof_view() throws IOException {
        adjustPath();
        ProfileParser parser = new ProfileParser("src/test/resources/case_test");
        Tree executionTree = new Tree(parser.getStartTime());
        
        for (ProfRow profRow : parser) {
            executionTree.addRow(profRow);
        }
        executionTree.finalizeTree();

        String[] cycleEntries = executionTree.traverse().split("\n");
        BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/golden.txt"));
        String line;
        String[] goldenEntries = reader.lines().toArray(String[]::new);
        
        assertEquals(cycleEntries.length, goldenEntries.length);
        for (int i = 0; i < cycleEntries.length; i++) {
            assertEquals(cycleEntries[i], goldenEntries[i]);
        }

        reader.close();
    }

    private void adjustPath() {
        // Function to adjust the PATH, if necessary
    }
}
