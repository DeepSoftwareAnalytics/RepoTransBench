import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Random;

public class NumericalUnitsTest {

    @BeforeEach
    public void setUp() {
        NumericalUnits.resetUnits("SI");
    }

    @Test
    public void testResetUnitsSi() {
        NumericalUnits.resetUnits("SI");
        assertEquals(1.0, NumericalUnits.m);
        assertEquals(1.0, NumericalUnits.kg);
        assertEquals(1.0, NumericalUnits.s);
        assertEquals(1.0, NumericalUnits.C);
        assertEquals(1.0, NumericalUnits.K);
    }

    @Test
    public void testResetUnitsRandom() {
        NumericalUnits.resetUnits();
        assertTrue(NumericalUnits.m >= 1e-2 && NumericalUnits.m <= 1e2);
        assertTrue(NumericalUnits.kg >= 1e-2 && NumericalUnits.kg <= 1e2);
        assertTrue(NumericalUnits.s >= 1e-2 && NumericalUnits.s <= 1e2);
        assertTrue(NumericalUnits.C >= 1e-2 && NumericalUnits.C <= 1e2);
        assertTrue(NumericalUnits.K >= 1e-2 && NumericalUnits.K <= 1e2);
    }

    @Test
    public void testResetUnitsPreserveRandomState() {
        Random random = new Random();
        long randomState = random.nextLong();
        NumericalUnits.resetUnits();
        assertEquals(randomState, random.nextLong());
    }

    @Test
    public void testNuEvalBasic() {
        assertDoesNotThrow(() -> {
            assertEquals(NumericalUnits.kg, NumericalUnits.nuEval("kg"), 1e-9);
            assertEquals(NumericalUnits.m, NumericalUnits.nuEval("m"), 1e-9);
            assertEquals(NumericalUnits.s, NumericalUnits.nuEval("s"), 1e-9);
        });
    }

    @Test
    public void testNuEvalComplex() {
        assertDoesNotThrow(() -> {
            assertEquals(NumericalUnits.kg * NumericalUnits.m / Math.pow(NumericalUnits.s, 2), NumericalUnits.nuEval("kg * m / s**2"), 1e-9);
            assertEquals(Math.pow(NumericalUnits.kg, -3.6), NumericalUnits.nuEval("kg**-3.6"), 1e-9);
        });
    }

    @Test
    public void testNuEvalInvalidExpression() {
        assertThrows(Exception.class, () -> {
            NumericalUnits.nuEval("kg + m");
        });
    }

    @Test
    public void testNuEvalNegativeExponent() {
        assertDoesNotThrow(() -> {
            assertEquals(Math.pow(NumericalUnits.m, -1), NumericalUnits.nuEval("m**-1"), 1e-9);
        });
    }

    @Test
    public void testNuEvalNoUnits() {
        assertDoesNotThrow(() -> {
            assertEquals(1, NumericalUnits.nuEval("1"), 1e-9);
            assertEquals(8, NumericalUnits.nuEval("2**3"), 1e-9);
        });
    }

    @Test
    public void testLengthUnits() {
        NumericalUnits.setDerivedUnitsAndConstants();
        assertEquals(1e-2 * NumericalUnits.m, NumericalUnits.cm, 1e-9);
    }

    @Test
    public void testVolumeUnits() {
        NumericalUnits.setDerivedUnitsAndConstants();
        assertEquals(1e-3 * Math.pow(NumericalUnits.m, 3), NumericalUnits.L, 1e-9);
    }

    @Test
    public void testTimeUnits() {
        NumericalUnits.setDerivedUnitsAndConstants();
        assertEquals(60.0 * NumericalUnits.s, NumericalUnits.minute, 1e-9);
    }

    @Test
    public void testFrequencyUnits() {
        NumericalUnits.setDerivedUnitsAndConstants();
        assertEquals(1.0 / NumericalUnits.s, NumericalUnits.Hz, 1e-9);
    }

    @Test
    public void testMassUnits() {
        NumericalUnits.setDerivedUnitsAndConstants();
        assertEquals(1e-3 * NumericalUnits.kg, NumericalUnits.g, 1e-9);
    }
}
