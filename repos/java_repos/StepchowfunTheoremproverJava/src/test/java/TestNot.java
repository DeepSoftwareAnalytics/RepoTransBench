import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;
import java.util.List;
public class TestNot {
    private Predicate formula;
    private Not notFormula;

    @BeforeEach
    public void setUp() {
        formula = new Predicate("P", List.of(new Variable("x")));
        notFormula = new Not(formula);
    }

    @Test
    public void testFreeVariables() {
        assertEquals(Set.of(new Variable("x")), notFormula.freeVariables());
    }

    @Test
    public void testReplace() {
        Not newNot = notFormula.replace(new Variable("x"), new Variable("y"));
        assertEquals("¬P(y)", newNot.toString());
    }

    @Test
    public void testOccurs() {
        assertFalse(notFormula.occurs(new UnificationTerm("u")));
    }

    @Test
    public void testEq() {
        assertTrue(notFormula.equals(new Not(new Predicate("P", List.of(new Variable("x"))))));
        assertFalse(notFormula.equals(new Not(new Predicate("Q", List.of()))));
    }

    @Test
    public void testHash() {
        assertEquals("¬P(x)".hashCode(), notFormula.hashCode());
    }
}
