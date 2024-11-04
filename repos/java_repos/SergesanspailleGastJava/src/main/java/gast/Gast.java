package gast;

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

public class Gast {
    private static PythonInterpreter interpreter = new PythonInterpreter();

    static {
        interpreter.exec("import ast");
        interpreter.exec("import gast");
    }

    public static PyObject parse(String code) {
        interpreter.set("code", code);
        return interpreter.eval("gast.parse(code)");
    }

    public static String unparse(PyObject gastObj) {
        interpreter.set("gast_obj", gastObj);
        return interpreter.eval("gast.unparse(gast_obj)").toString();
    }

    public static PyObject literalEval(PyObject nodeOrString) {
        interpreter.set("node_or_string", nodeOrString);
        return interpreter.eval("gast.literal_eval(node_or_string)");
    }

    public static String getDocstring(PyObject node, boolean clean) {
        interpreter.set("node", node);
        interpreter.set("clean", clean);
        return interpreter.eval("gast.get_docstring(node, clean)").toString();
    }

    public static PyObject copyLocation(PyObject newNode, PyObject oldNode) {
        interpreter.set("new_node", newNode);
        interpreter.set("old_node", oldNode);
        return interpreter.eval("gast.copy_location(new_node, old_node)");
    }

    public static PyObject fixMissingLocations(PyObject node) {
        interpreter.set("node", node);
        return interpreter.eval("gast.fix_missing_locations(node)");
    }

    public static PyObject incrementLineno(PyObject node, int n) {
        interpreter.set("node", node);
        interpreter.set("n", n);
        return interpreter.eval("gast.increment_lineno(node, n)");
    }
}
