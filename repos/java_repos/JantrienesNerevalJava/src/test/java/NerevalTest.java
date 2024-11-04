import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;


public class NerevalTest {

    @Test
    public void testHasOverlap() {
        Nereval.Entity a = new Nereval.Entity("CILINDRISCHE PLUG", "Productname", 0);
        Nereval.Entity b = new Nereval.Entity("PLUG", "Productname", 13);
        assertTrue(Nereval.hasOverlap(a, b));
        assertTrue(Nereval.hasOverlap(b, a));

        b = new Nereval.Entity("PLUG", "Productname", 18);
        assertFalse(Nereval.hasOverlap(a, b));
    }

    @Test
    public void testHasOverlapOpenInterval() {
        Nereval.Entity a = new Nereval.Entity("PLUG", "Productname", 0);
        Nereval.Entity b = new Nereval.Entity("AB", "Productname", 4);
        assertFalse(Nereval.hasOverlap(a, b));
        assertFalse(Nereval.hasOverlap(b, a));
    }

    @Test
    public void testEntity() {
        Nereval.Entity e = new Nereval.Entity("CILINDRISCHE PLUG", "Productname", 0);
        assertEquals("CILINDRISCHE PLUG", e.getText());
        assertEquals("Productname", e.getType());
        assertEquals(0, e.getStart());
    }

    @Test
    public void testCorrectTextSymmetry() {
        Nereval.Entity trueEntity = new Nereval.Entity("CILINDRISCHE PLUG", "Productname", 0);
        Nereval.Entity pred = new Nereval.Entity("CILINDRISCHE", "Productname", 0);
        assertFalse(Nereval.correctText(trueEntity, pred));
        assertFalse(Nereval.correctText(pred, trueEntity));
        assertTrue(Nereval.correctText(trueEntity, trueEntity));
        assertTrue(Nereval.correctText(pred, pred));
    }

    @Test
    public void testCorrectTextWithoutOverlap() {
        Nereval.Entity trueEntity = new Nereval.Entity("CILINDRISCHE PLUG", "Productname", 0);
        Nereval.Entity pred = new Nereval.Entity("CILINDRISCHE PLUG", "Productname", 11);
        assertFalse(Nereval.correctText(trueEntity, pred));
    }

    @Test
    public void testCorrectTextTypeMismatch() {
        Nereval.Entity trueEntity = new Nereval.Entity("a", "Productname", 0);
        Nereval.Entity pred = new Nereval.Entity("a", "Material", 0);
        assertTrue(Nereval.correctText(trueEntity, pred));
    }

    @Test
    public void testCorrectTypeSymmetry() {
        Nereval.Entity trueEntity = new Nereval.Entity("CILINDRISCHE PLUG", "Productname", 0);
        Nereval.Entity pred = new Nereval.Entity("PLUG", "Productname", 13);
        assertTrue(Nereval.correctType(trueEntity, pred));
        assertTrue(Nereval.correctType(pred, trueEntity));
        assertTrue(Nereval.correctType(trueEntity, trueEntity));
        assertTrue(Nereval.correctType(pred, pred));
    }

    @Test
    public void testCorrectTypeWithOverlap() {
        Nereval.Entity trueEntity = new Nereval.Entity("CILINDRISCHE", "Productname", 0);
        Nereval.Entity pred = new Nereval.Entity("CILINDRISCHE PLUG", "Productname", 0);
        assertTrue(Nereval.correctType(trueEntity, pred));
    }

    @Test
    public void testCorrectTypeWithoutOverlap() {
        Nereval.Entity trueEntity = new Nereval.Entity("PLUG", "Productname", 0);
        Nereval.Entity pred = new Nereval.Entity("CILINDRISCHE PLUG", "Productname", 21);
        assertFalse(Nereval.correctType(trueEntity, pred));
    }

    @Test
    public void testCorrectTypeWithMismatch() {
        Nereval.Entity trueEntity = new Nereval.Entity("PLUG", "Productname", 0);
        Nereval.Entity pred = new Nereval.Entity("PLUG", "Material", 0);
        assertFalse(Nereval.correctType(trueEntity, pred));
    }

    @Test
    public void testCountCorrect() {
        Nereval.Entity[] x = {
            new Nereval.Entity("CILINDRISCHE PLUG", "Productname", 0),
            new Nereval.Entity("DIN908", "Productname", 18),
            new Nereval.Entity("M10X1", "Dimension", 25)
        };

        Nereval.Entity[] y = {
            new Nereval.Entity("CILINDRISCHE", "Productname", 0),
            new Nereval.Entity("PLUG", "Productname", 13),
            new Nereval.Entity("DIN908", "Productname", 18),
            new Nereval.Entity("M10X1", "Productname", 25),
            new Nereval.Entity("foo", "Productname", 35)
        };

        int[] counts = Nereval.countCorrect(Arrays.asList(x), Arrays.asList(y));
        assertEquals(2, counts[0]); // correct text
        assertEquals(2, counts[1]); // correct type

        counts = Nereval.countCorrect(Arrays.asList(y), Arrays.asList(x));
        assertEquals(2, counts[0]);
        assertEquals(3, counts[1]);

        counts = Nereval.countCorrect(Collections.emptyList(), Collections.emptyList());
        assertEquals(0, counts[0]);
        assertEquals(0, counts[1]);
    }

    @Test
    public void testPrecision() {
        assertEquals(0, Nereval.precision(0, 10));
        assertEquals(0, Nereval.precision(0, 0));
        assertEquals(1, Nereval.precision(10, 10));
        assertEquals(0.5, Nereval.precision(5, 10));
    }

    @Test
    public void testRecall() {
        assertEquals(0, Nereval.recall(0, 0));
        assertEquals(0, Nereval.recall(0, 10));
        assertEquals(1, Nereval.recall(10, 10));
        assertEquals(0.5, Nereval.recall(5, 10));
    }

    @Test
    public void testEvaluate() {
        Nereval.Entity[] x = {
            new Nereval.Entity("CILINDRISCHE PLUG", "Productname", 0),
            new Nereval.Entity("DIN908", "Productname", 18),
            new Nereval.Entity("M10X1", "Dimension", 25)
        };

        Nereval.Entity[] y = {
            new Nereval.Entity("CILINDRISCHE", "Productname", 0),
            new Nereval.Entity("PLUG", "Productname", 13),
            new Nereval.Entity("DIN908", "Productname", 18),
            new Nereval.Entity("M10X1", "Productname", 25),
            new Nereval.Entity("foo", "Productname", 35)
        };

        List<Nereval.Entity> xList1 = Arrays.asList(x);
        List<Nereval.Entity> yList1 = Arrays.asList(y);

        assertEquals(0.5, Nereval.evaluate(Arrays.asList(xList1), Arrays.asList(yList1)));
        assertEquals(0.625, Nereval.evaluate(Arrays.asList(yList1), Arrays.asList(xList1)));
        assertEquals(1, Nereval.evaluate(Arrays.asList(xList1, yList1), Arrays.asList(xList1, yList1)));
        assertEquals(0.5625, Nereval.evaluate(Arrays.asList(xList1, yList1), Arrays.asList(yList1, xList1)));
        assertEquals(0, Nereval.evaluate(Arrays.asList(xList1), Arrays.asList(Collections.emptyList())));
        assertEquals(0, Nereval.evaluate(Arrays.asList(Collections.emptyList()), Arrays.asList(xList1)));
    }

    @Test
    public void testEvaluateDifferentShapes() {
        assertThrows(IllegalArgumentException.class, () -> {
            Nereval.evaluate(Arrays.asList(Collections.emptyList(), Collections.emptyList()), 
                             Arrays.asList(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        });
    }

    @Test
    public void testSignTest() {
        Nereval.Entity x = new Nereval.Entity("CILINDRISCHE PLUG", "Productname", 0);
        Nereval.Entity y = new Nereval.Entity("CILINDRISCHE", "Productname", 0);
        
        int[] result1 = Nereval.signTest(
            Arrays.asList(Arrays.asList(x)), 
            Arrays.asList(Arrays.asList(x)), 
            Arrays.asList(Arrays.asList(y))
        );
        assertEquals(0, result1[0]);
        assertEquals(1, result1[1]);

        int[] result2 = Nereval.signTest(
            Arrays.asList(Arrays.asList(x)), 
            Arrays.asList(Arrays.asList(y)), 
            Arrays.asList(Arrays.asList(x))
        );
        assertEquals(1, result2[0]);
        assertEquals(0, result2[1]);

        int[] result3 = Nereval.signTest(
            Arrays.asList(Arrays.asList(x)), 
            Arrays.asList(Arrays.asList(x)), 
            Arrays.asList(Arrays.asList(x))
        );
        assertEquals(0, result3[0]);
        assertEquals(0, result3[1]);

        int[] result4 = Nereval.signTest(
            Arrays.asList(Arrays.asList(x, y)), 
            Arrays.asList(Collections.<List<Nereval.Entity>>emptyList(), Collections.<List<Nereval.Entity>>emptyList()), 
            Arrays.asList(Arrays.asList(x, y))
        );

        assertEquals(2, result4[0]);
        assertEquals(0, result4[1]);

        int[] result5 = Nereval.signTest(
            Arrays.asList(Arrays.asList(x, y)), 
            Arrays.asList(Arrays.asList(x, y)), 
            Arrays.asList(Collections.<List<Nereval.Entity>>emptyList(), Collections.<List<Nereval.Entity>>emptyList())
        );
        assertEquals(0, result5[0]);
        assertEquals(2, result5[1]);
    }

    @Test
    public void testParseJson() throws IOException {
        String fileName = getClass().getClassLoader().getResource("input.json").getFile();
        List<Map<String, Object>> predictions = Nereval.parseJson(fileName);
        assertEquals(1, predictions.size());

        List<Nereval.Entity> trueEntities = (List<Nereval.Entity>) predictions.get(0).get("true");
        List<Nereval.Entity> predictedEntities = (List<Nereval.Entity>) predictions.get(0).get("predicted");

        assertEquals("CILINDRISCHE PLUG", trueEntities.get(0).getText());
        assertEquals(new Nereval.Entity("CILINDRISCHE PLUG", "Productname", 0), trueEntities.get(0));
        assertEquals(new Nereval.Entity("CILINDRISCHE", "Productname", 0), predictedEntities.get(0));
        assertEquals(new Nereval.Entity("PLUG", "Productname", 13), predictedEntities.get(1));
    }

    @Test
    public void testEvaluateJson() throws IOException {
        String fileName = getClass().getClassLoader().getResource("input.json").getFile();
        double result = Nereval.evaluateJson(fileName);
        assertEquals(0.0, result, 0.0001);  // 使用delta进行浮点数比较
    }
}
