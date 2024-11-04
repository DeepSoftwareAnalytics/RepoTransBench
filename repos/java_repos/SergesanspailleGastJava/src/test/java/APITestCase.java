import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class APITestCase {

    private PythonInterpreter interpreter;

    @BeforeEach
    public void setUp() {
        interpreter = new PythonInterpreter();
        interpreter.exec("import ast");
        interpreter.exec("import gast");
    }

    @Test
    public void testLiteralEvalString() {
        interpreter.exec("code = '1, 3'");
        PyObject evalCode = interpreter.eval("ast.literal_eval(code)");
        PyObject evalGast = interpreter.eval("gast.literal_eval(code)");
        assertEquals(evalCode, evalGast);
    }

    @Test
    public void testLiteralEvalCode() {
        interpreter.exec("code = '[1, 3]'");
        interpreter.exec("tree = ast.parse(code, mode='eval')");
        interpreter.exec("gtree = gast.parse(code, mode='eval')");
        PyObject evalCode = interpreter.eval("ast.literal_eval(tree)");
        PyObject evalGast = interpreter.eval("gast.literal_eval(gtree)");
        assertEquals(evalCode, evalGast);
    }

    @Test
    public void testParse() {
        interpreter.exec("code = '''\n" +
                "def foo(x=1, *args, **kwargs):\n" +
                "    return x + y + len(args) + len(kwargs)\n" +
                "'''");
        interpreter.exec("gast.parse(code)");
    }

    @Test
    public void testUnparse() {
        interpreter.exec("code = 'def foo(x=1): return x'");
        PyObject evalUnparse = interpreter.eval("gast.unparse(gast.parse(code))");
        assertEquals(evalUnparse.toString(), "def foo(x=1):\n    return x");
    }

    @Test
    public void testDump() {
        interpreter.exec("code = 'lambda x: x'");
        interpreter.exec("tree = gast.parse(code, mode='eval')");
        interpreter.exec("def dump(node):\n" +
                "    return gast.dump(node, show_empty=True)");
        PyObject evalDump = interpreter.eval("dump(tree)");
        String norm = "Expression(body=Lambda(args=arguments(args=[Name(id='x', ctx=Param(), annotation=None, type_comment=None)], posonlyargs=[], vararg=None, kwonlyargs=[], kw_defaults=[], kwarg=None, defaults=[]), body=Name(id='x', ctx=Load(), annotation=None, type_comment=None)))";
        assertEquals(evalDump.toString(), norm);
    }

    @Test
    public void testWalk() {
        interpreter.exec("code = 'x + 1'");
        interpreter.exec("tree = gast.parse(code, mode='eval')");
        interpreter.exec("def dump(node):\n" +
                "    return gast.dump(node, show_empty=True)");
        PyObject evalDump = interpreter.eval("dump(tree)");
        String norm = "Expression(body=BinOp(left=Name(id='x', ctx=Load(), annotation=None, type_comment=None), op=Add(), right=Constant(value=1, kind=None)))";
        assertEquals(evalDump.toString(), norm);
        assertEquals(interpreter.eval("len(list(gast.walk(tree)))").asInt(), 6);
    }

    @Test
    public void testIterFields() {
        interpreter.exec("tree = gast.Constant(value=1, kind=None)");
        PyObject fieldNamesPy = interpreter.eval("set(name for name, _ in gast.iter_fields(tree))");
        Set<String> fieldNames = new HashSet<>();
        for (PyObject fieldName : fieldNamesPy.asIterable()) {
            fieldNames.add(fieldName.asString());
        }
        assertEquals(new HashSet<>(Arrays.asList("value", "kind")), fieldNames);
    }

    @Test
    public void testIterChildNodes() {
        interpreter.exec("tree = gast.UnaryOp(gast.USub(), gast.Constant(value=1, kind=None))");
        PyObject childNodes = interpreter.eval("list(gast.iter_child_nodes(tree))");
        assertEquals(childNodes.__len__(), 2);
    }

    @Test
    public void testIncrementLineno() {
        interpreter.exec("tree = gast.Constant(value=1, kind=None)");
        interpreter.exec("tree.lineno = 1");
        interpreter.exec("gast.increment_lineno(tree)");
        assertEquals(interpreter.eval("tree.lineno").asInt(), 2);
    }

    @Test
    public void testGetDocstring() {
        interpreter.exec("code = 'def foo(): \"foo\"'");
        interpreter.exec("tree = gast.parse(code)");
        PyObject func = interpreter.eval("tree.body[0]");
        PyObject docs = interpreter.eval("gast.get_docstring(func)");
        assertEquals(docs.asString(), "foo");
    }

    @Test
    public void testCopyLocation() {
        interpreter.exec("tree = gast.Constant(value=1, kind=None)");
        interpreter.exec("tree.lineno = 1");
        interpreter.exec("tree.col_offset = 2");

        interpreter.exec("node = gast.Constant(value=2, kind=None)");
        interpreter.exec("gast.copy_location(node, tree)");
        assertEquals(interpreter.eval("node.lineno").asInt(), interpreter.eval("tree.lineno").asInt());
        assertEquals(interpreter.eval("node.col_offset").asInt(), interpreter.eval("tree.col_offset").asInt());
    }

    @Test
    public void testFixMissingLocations() {
        interpreter.exec("node = gast.Constant(value=6, kind=None)");
        interpreter.exec("tree = gast.UnaryOp(gast.USub(), node)");
        interpreter.exec("tree.lineno = 1");
        interpreter.exec("tree.col_offset = 2");
        interpreter.exec("gast.fix_missing_locations(tree)");
        assertEquals(interpreter.eval("node.lineno").asInt(), interpreter.eval("tree.lineno").asInt());
        assertEquals(interpreter.eval("node.col_offset").asInt(), interpreter.eval("tree.col_offset").asInt());
    }

    @Test
    public void testNodeTransformer() {
        interpreter.exec("node = gast.Constant(value=6, kind=None)");
        interpreter.exec("tree = gast.UnaryOp(gast.USub(), node)");

        interpreter.exec("class Trans(gast.NodeTransformer):\n" +
                "    def visit_Constant(self, node):\n" +
                "        node.value *= 2\n" +
                "        return node");

        interpreter.exec("tree = Trans().visit(tree)");
        assertEquals(interpreter.eval("node.value").asInt(), 12);
    }

    @Test
    public void testNodeVisitor() {
        interpreter.exec("node = gast.Constant(value=6, kind=None)");
        interpreter.exec("tree = gast.UnaryOp(gast.USub(), node)");

        interpreter.exec("class Vis(gast.NodeTransformer):\n" +
                "    def __init__(self):\n" +
                "        self.state = []\n" +
                "    def visit_Constant(self, node):\n" +
                "        self.state.append(node.value)");

        interpreter.exec("vis = Vis()");
        interpreter.exec("vis.visit(tree)");
        PyObject state = interpreter.eval("vis.state");
        assertEquals(state.__getitem__(0).asInt(), 6);
    }

    @Test
    public void testNodeConstructor() {
        interpreter.exec("node0 = gast.Name()");
        interpreter.exec("load = gast.Load()");
        interpreter.exec("node1 = gast.Name('id', load, None, None)");
        interpreter.exec("node2 = gast.Name('id', load, None, type_comment=None)");

        Exception exception = assertThrows(PyException.class, () -> {
            interpreter.exec("gast.Name('id', 'ctx', 'annotation', 'type_comment', 'random_field')");
        });
        assertTrue(exception.getMessage().contains("TypeError"));

        String[] fields = {"id", "ctx", "annotation", "type_comment"};
        for (String field : fields) {
            assertEquals(interpreter.eval("getattr(node1, '" + field + "')").toString(), interpreter.eval("getattr(node2, '" + field + "')").toString());
        }
    }
}
