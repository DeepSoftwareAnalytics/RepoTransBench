import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;
import java.util.List;
public class TestFunction {
    private Function func1;
    private Function func2;
    private Function func3;

    @BeforeEach
    public void setUp() {
        func1 = new Function("f", List.of(new Variable("x")));
        func2 = new Function("g", List.of());
        func3 = new Function("f", List.of(new Variable("x")));
    }

    @Test
    public void testFreeVariables() {
        assertEquals(Set.of(new Variable("x")), func1.freeVariables());
        assertEquals(Set.of(), func2.freeVariables());
    }

    @Test
    public void testReplace() {
        Function newFunc = func1.replace(new Variable("x"), new Variable("y"));
        assertEquals("f(y)", newFunc.toString());
    }

    @Test
    public void testOccurs() {
        assertFalse(func1.occurs(new UnificationTerm("u")));
    }

    @Test
    public void testEq() {
        assertTrue(func1.equals(func3));
        assertFalse(func1.equals(func2));
    }

    @Test
    public void testHash() {
        assertEquals("f(x)".hashCode(), func1.hashCode());
    }
}
