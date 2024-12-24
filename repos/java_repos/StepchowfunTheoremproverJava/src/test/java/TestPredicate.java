import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;
import java.util.List;
public class TestPredicate {
    private Predicate pred1;
    private Predicate pred2;
    private Predicate pred3;

    @BeforeEach
    public void setUp() {
        pred1 = new Predicate("P", List.of(new Variable("x")));
        pred2 = new Predicate("Q", List.of());
        pred3 = new Predicate("P", List.of(new Variable("x")));
    }

    @Test
    public void testFreeVariables() {
        assertEquals(Set.of(new Variable("x")), pred1.freeVariables());
        assertEquals(Set.of(), pred2.freeVariables());
    }

    @Test
    public void testReplace() {
        Predicate newPred = pred1.replace(new Variable("x"), new Variable("y"));
        assertEquals("P(y)", newPred.toString());
    }

    @Test
    public void testOccurs() {
        assertFalse(pred1.occurs(new UnificationTerm("u")));
    }

    @Test
    public void testEq() {
        assertTrue(pred1.equals(pred3));
        assertFalse(pred1.equals(pred2));
    }

    @Test
    public void testHash() {
        assertEquals("P(x)".hashCode(), pred1.hashCode());
    }
}
