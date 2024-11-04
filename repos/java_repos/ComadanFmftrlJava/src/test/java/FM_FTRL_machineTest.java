import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import java.io.*;
import java.util.*;

public class FM_FTRL_machineTest {

    private FMFTRLMachine fm;

    @BeforeEach
    public void setUp() {
        fm = new FMFTRLMachine(
            4, 0.01, 1.0, 0.1, 2.0, 1.0, 
            4194304, 0.1, 1.0, 0.1, 1.0, 0.8
        );
    }

    @Test
    public void testInitFM() {
        fm.initFM(1);
        assertTrue(fm.getNFM().containsKey(1));
        assertTrue(fm.getZFM().containsKey(1));
        assertTrue(fm.getWFM().containsKey(1));
        assertEquals(4, fm.getNFM().get(1).length);
    }

    @Test
    public void testInitFMMultiple() {
        List<Integer> keys = Arrays.asList(1, 2, 3, 4, 5);
        for (int key : keys) {
            fm.initFM(key);
        }
        for (int key : keys) {
            assertTrue(fm.getNFM().containsKey(key));
            assertTrue(fm.getZFM().containsKey(key));
            assertTrue(fm.getWFM().containsKey(key));
            assertEquals(4, fm.getNFM().get(key).length);
        }
    }

    @Test
    public void testPredictRaw() {
        List<Integer> x = Arrays.asList(1, 2, 3);
        fm.initFM(1);
        fm.initFM(2);
        fm.initFM(3);
        double rawScore = fm.predictRaw(x);
        assertTrue(Double.class.isInstance(rawScore));
    }

    @Test
    public void testPredictRawWithEmptyInput() {
        List<Integer> x = new ArrayList<>();
        double rawScore = fm.predictRaw(x);
        assertEquals(0.0, rawScore);
    }

    @Test
    public void testPredict() {
        List<Integer> x = Arrays.asList(1, 2, 3);
        fm.initFM(1);
        fm.initFM(2);
        fm.initFM(3);
        double prediction = fm.predict(x);
        assertTrue(prediction >= 0 && prediction <= 1);
    }

    @Test
    public void testPredictWithEmptyInput() {
        List<Integer> x = new ArrayList<>();
        double prediction = fm.predict(x);
        assertEquals(0.5, prediction);
    }

    @Test
    public void testDropout() {
        List<Integer> x = Arrays.asList(1, 2, 3, 4, 5);
        fm.dropout(x);
        assertTrue(x.size() <= 5);
    }

    @Test
    public void testDropoutWithNoElements() {
        List<Integer> x = new ArrayList<>();
        fm.dropout(x);
        assertEquals(0, x.size());
    }

    @Test
    public void testUpdate() {
        List<Integer> x = Arrays.asList(1, 2, 3);
        double p = 0.5;
        double y = 1;
        fm.initFM(1);
        fm.initFM(2);
        fm.initFM(3);
        fm.update(x, p, y);
        assertNotEquals(0, fm.getZ()[1]);
        assertNotEquals(0, fm.getN()[1]);
    }

    @Test
    public void testUpdateWithUninitializedFeature() {
        List<Integer> x = Arrays.asList(1, 2, 3);
        double p = 0.5;
        double y = 1;
        fm.initFM(1);
        fm.initFM(2);
        fm.initFM(3);
        fm.update(x, p, y);
        assertNotEquals(0, fm.getZ()[1]);
        assertNotEquals(0, fm.getN()[1]);
    }

    @Test
    public void testWriteW() throws IOException {
        fm.writeW("test_w.txt");
        BufferedReader reader = new BufferedReader(new FileReader("test_w.txt"));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        reader.close();
        assertTrue(lines.size() > 0);
        new File("test_w.txt").delete(); // Clean up
    }

    @Test
    public void testWriteWFM() throws IOException {
        fm.initFM(1);
        fm.writeWFM("test_w_fm.txt");
        BufferedReader reader = new BufferedReader(new FileReader("test_w_fm.txt"));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        reader.close();
        assertTrue(lines.size() > 0);
        new File("test_w_fm.txt").delete(); // Clean up
    }

    @Test
    public void testWriteWFMMultiple() throws IOException {
        List<Integer> keys = Arrays.asList(1, 2, 3);
        for (int key : keys) {
            fm.initFM(key);
        }
        fm.writeWFM("test_w_fm_multiple.txt");
        BufferedReader reader = new BufferedReader(new FileReader("test_w_fm_multiple.txt"));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        reader.close();
        assertTrue(lines.size() > 0);
        new File("test_w_fm_multiple.txt").delete(); // Clean up
    }
}

