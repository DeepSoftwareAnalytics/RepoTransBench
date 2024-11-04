import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;
import java.util.List;
public class TestUnificationTerm {
    private UnificationTerm term1;
    private UnificationTerm term2;

    @BeforeEach
    public void setUp() {
        term1 = new UnificationTerm("t1");
        term2 = new UnificationTerm("t2");
    }

    @Test
    public void testFreeVariables() {
        assertEquals(Set.of(), term1.freeVariables());
    }

    @Test
    public void testReplace() {
        assertEquals(term2, term1.replace(term1, term2));
        assertEquals(term1, term1.replace(term2, term1));
    }

    @Test
    public void testOccurs() {
        assertTrue(term1.occurs(term1));
    }

    @Test
    public void testEq() {
        assertTrue(term1.equals(new UnificationTerm("t1")));
        assertFalse(term1.equals(term2));
    }

    @Test
    public void testHash() {
        assertEquals("t1".hashCode(), term1.hashCode());
    }
}
