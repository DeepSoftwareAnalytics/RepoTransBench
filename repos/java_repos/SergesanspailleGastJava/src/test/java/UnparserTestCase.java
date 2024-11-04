import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnparserTestCase {

    private PythonInterpreter interpreter;

    @BeforeEach
    public void setUp() {
        interpreter = new PythonInterpreter();
        interpreter.exec("import ast");
        interpreter.exec("import gast");
    }

    public void assertUnparse(String code) {
        interpreter.exec("normalized_code = ast.unparse(ast.parse(\"" + code + "\"))");
        interpreter.exec("tree = gast.parse(normalized_code)");
        interpreter.exec("compile(gast.gast_to_ast(tree), '<test>', 'exec')");
        PyObject unparsed = interpreter.eval("gast.unparse(tree)");
        PyObject normalizedCode = interpreter.eval("normalized_code");
        assertEquals(normalizedCode.toString(), unparsed.toString());
    }

    @Test
    public void testFunctionDef() {
        assertUnparse("def foo(x, y): return x, y");
    }

    @Test
    public void testBinaryOp() {
        assertUnparse("1 + 3");
    }
}
