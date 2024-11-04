import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ComputeTest {
    @Test
    void testAddition() throws Exception {
        Node ast = Parser.parse("1+1");
        assertEquals(2, Compute.compute(ast));

        ast = Parser.parse("2+3");
        assertEquals(5, Compute.compute(ast));

        ast = Parser.parse("0+0");
        assertEquals(0, Compute.compute(ast));

        ast = Parser.parse("1+0");
        assertEquals(1, Compute.compute(ast));

        ast = Parser.parse("0+6");
        assertEquals(6, Compute.compute(ast));
    }

    @Test
    void testSubtraction() throws Exception {
        Node ast = Parser.parse("1-1");
        assertEquals(0, Compute.compute(ast));

        ast = Parser.parse("5-3");
        assertEquals(2, Compute.compute(ast));

        ast = Parser.parse("0-0");
        assertEquals(0, Compute.compute(ast));

        ast = Parser.parse("3-0");
        assertEquals(3, Compute.compute(ast));

        ast = Parser.parse("0-3");
        assertEquals(-3, Compute.compute(ast));
    }

    @Test
    void testMultiplication() throws Exception {
        Node ast = Parser.parse("1*2");
        assertEquals(2, Compute.compute(ast));

        ast = Parser.parse("2*3");
        assertEquals(6, Compute.compute(ast));

        ast = Parser.parse("0*1");
        assertEquals(0, Compute.compute(ast));

        ast = Parser.parse("4*0");
        assertEquals(0, Compute.compute(ast));

        ast = Parser.parse("0*0");
        assertEquals(0, Compute.compute(ast));
    }

    @Test
    void testDivision() throws Exception {
        Node ast = Parser.parse("8/4/2");
        assertEquals(1, Compute.compute(ast));

        ast = Parser.parse("7/4");
        assertEquals(1.75, Compute.compute(ast));

        ast = Parser.parse("1/1");
        assertEquals(1, Compute.compute(ast));

        ast = Parser.parse("0/1");
        assertEquals(0, Compute.compute(ast));

        ast = Parser.parse("6/2/3");
        assertEquals(1, Compute.compute(ast));
    }

    @Test
    void testComplexExpression() throws Exception {
        Node ast = Parser.parse("3-2+1");
        assertEquals(2, Compute.compute(ast));

        ast = Parser.parse("(1+7)*(9+2)");
        assertEquals(88, Compute.compute(ast));

        ast = Parser.parse("(2+7)/4");
        assertEquals(2.25, Compute.compute(ast));

        ast = Parser.parse("2*3+4");
        assertEquals(10, Compute.compute(ast));

        ast = Parser.parse("2*(3+4)");
        assertEquals(14, Compute.compute(ast));

        ast = Parser.parse("2+3*4");
        assertEquals(14, Compute.compute(ast));

        ast = Parser.parse("2+(3*4)");
        assertEquals(14, Compute.compute(ast));

        ast = Parser.parse("2-(3*4+1)");
        assertEquals(-11, Compute.compute(ast));

        ast = Parser.parse("2*(3*4+1)");
        assertEquals(26, Compute.compute(ast));

        ast = Parser.parse("8/((1+3)*2)");
        assertEquals(1, Compute.compute(ast));

        ast = Parser.parse("(2+3)*(4+5)");
        assertEquals(45, Compute.compute(ast));

        ast = Parser.parse("(1-1)*(8+8)");
        assertEquals(0, Compute.compute(ast));

        ast = Parser.parse("(2*3)+(4/2)");
        assertEquals(8, Compute.compute(ast));

        ast = Parser.parse("(5-2)*(3+1)");
        assertEquals(12, Compute.compute(ast));
    }

    @Test
    void testInvalidExpressions() {
        String[] invalidExpressions = new String[]{
                "1+1)",  // Unmatched closing parenthesis
                "2*(3+2",  // Unmatched opening parenthesis
                "5++5",  // Double operator
                "(4/2",  // Unmatched opening parenthesis with division
                "8//2"  // Double division operator
        };

        for (String expr : invalidExpressions) {
            assertThrows(Exception.class, () -> {
                Node ast = Parser.parse(expr);
                Compute.compute(ast);
            });
        }
    }
}
