import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;
import java.util.List;
public class TestVariable {
    private Variable var1;
    private Variable var2;

    @BeforeEach
    public void setUp() {
        var1 = new Variable("x");
        var2 = new Variable("y");
    }

    @Test
    public void testFreeVariables() {
        assertEquals(Set.of(var1), var1.freeVariables());
    }

    @Test
    public void testReplace() {
        assertEquals(var2, var1.replace(var1, var2));
        assertEquals(var1, var1.replace(var2, var1));
    }

    @Test
    public void testOccurs() {
        assertFalse(var1.occurs(new UnificationTerm("u")));
    }

    @Test
    public void testEq() {
        assertTrue(var1.equals(new Variable("x")));
        assertFalse(var1.equals(var2));
    }

    @Test
    public void testHash() {
        assertEquals("x".hashCode(), var1.hashCode());
    }
}
